package dev.slethware.walletservice.service.wallet;

import com.google.gson.Gson;
import dev.slethware.walletservice.exception.BadRequestException;
import dev.slethware.walletservice.exception.ResourceNotFoundException;
import dev.slethware.walletservice.models.dtos.request.DepositRequest;
import dev.slethware.walletservice.models.dtos.request.TransferRequest;
import dev.slethware.walletservice.models.dtos.request.WithdrawRequest;
import dev.slethware.walletservice.models.dtos.response.*;
import dev.slethware.walletservice.models.entity.Transaction;
import dev.slethware.walletservice.models.entity.User;
import dev.slethware.walletservice.models.entity.Wallet;
import dev.slethware.walletservice.models.enums.TransactionStatus;
import dev.slethware.walletservice.models.enums.TransactionType;
import dev.slethware.walletservice.repository.TransactionRepository;
import dev.slethware.walletservice.repository.UserRepository;
import dev.slethware.walletservice.repository.WalletRepository;
import dev.slethware.walletservice.service.paystack.PaystackService;
import dev.slethware.walletservice.utility.WalletNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PaystackService paystackService;

    @Override
    @Transactional
    public void createWalletForUser(User user) {
        if (walletRepository.findByUserId(user.getId()).isPresent()) {
            log.info("Wallet already exists for user: {}", user.getEmail());
            return;
        }

        String walletNumber;
        do {
            walletNumber = WalletNumberGenerator.generate();
        } while (walletRepository.existsByWalletNumber(walletNumber));

        Wallet wallet = Wallet.builder()
                .user(user)
                .walletNumber(walletNumber)
                .balance(0L)
                .build();

        walletRepository.save(wallet);
        log.info("Created wallet {} for user: {}", walletNumber, user.getEmail());
    }

    @Override
    @Transactional
    public ApiResponse<DepositResponse> initiateDeposit(DepositRequest request) {
        User currentUser = getCurrentUser();
        Wallet wallet = getWalletByUser(currentUser);

        String reference = paystackService.generateReference();

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                .status(TransactionStatus.PENDING)
                .reference(reference)
                .metadata(new HashMap<>())
                .build();

        transactionRepository.save(transaction);

        String authorizationUrl = paystackService.initializeTransaction(
                currentUser.getEmail(),
                request.getAmount(),
                reference
        );

        DepositResponse depositResponse = DepositResponse.builder()
                .reference(reference)
                .authorizationUrl(authorizationUrl)
                .build();

        return ApiResponse.<DepositResponse>builder()
                .status("success")
                .statusCode(200)
                .message("Deposit initiated successfully")
                .data(depositResponse)
                .build();
    }

    @Override
    @Transactional
    public void processWebhook(String payload, String signature) {
        if (!paystackService.verifyWebhookSignature(payload, signature)) {
            throw new BadRequestException("Invalid webhook signature");
        }

        com.google.gson.Gson gson = new com.google.gson.Gson();
        java.util.Map<String, Object> payloadMap = gson.fromJson(payload, java.util.Map.class);

        String event = (String) payloadMap.get("event");

        if (!"charge.success".equals(event)) {
            log.info("Ignoring webhook event: {}", event);
            return;
        }

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) payloadMap.get("data");
        String reference = (String) data.get("reference");
        String status = (String) data.get("status");
        Long amountInKobo = ((Number) data.get("amount")).longValue();

        Transaction transaction = transactionRepository.findByReferenceForUpdate(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.info("Transaction {} already processed", reference);
            return;
        }

        if ("success".equals(status)) {
            if (!transaction.getAmount().equals(amountInKobo)) {
                log.error("Amount mismatch for transaction {}: expected {}, got {}",
                        reference, transaction.getAmount(), amountInKobo);
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                return;
            }

            Wallet wallet = transaction.getWallet();
            wallet.setBalance(wallet.getBalance() + amountInKobo);
            walletRepository.save(wallet);

            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);

            log.info("Credited wallet {} with {} kobo", wallet.getWalletNumber(), amountInKobo);
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.info("Transaction {} failed", reference);
        }
    }

    @Override
    public ApiResponse<DepositStatusResponse> getDepositStatus(String reference) {
        Transaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        DepositStatusResponse statusResponse = DepositStatusResponse.builder()
                .reference(transaction.getReference())
                .status(transaction.getStatus().name().toLowerCase())
                .amount(transaction.getAmountInNaira())
                .build();

        return ApiResponse.<DepositStatusResponse>builder()
                .status("success")
                .statusCode(200)
                .message("Transaction status retrieved successfully")
                .data(statusResponse)
                .build();
    }

    @Override
    public ApiResponse<BalanceResponse> getBalance() {
        User currentUser = getCurrentUser();
        Wallet wallet = getWalletByUser(currentUser);

        BalanceResponse balanceResponse = BalanceResponse.builder()
                .balance(wallet.getBalanceInNaira())
                .walletNumber(wallet.getWalletNumber())
                .build();

        return ApiResponse.<BalanceResponse>builder()
                .status("success")
                .statusCode(200)
                .message("Balance retrieved successfully")
                .data(balanceResponse)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<TransferResponse> transfer(TransferRequest request) {
        User currentUser = getCurrentUser();
        Wallet senderWallet = walletRepository.findByUserIdForUpdate(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Wallet recipientWallet = walletRepository.findByWalletNumberForUpdate(request.getWalletNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient wallet not found"));

        if (senderWallet.getId().equals(recipientWallet.getId())) {
            throw new BadRequestException("Cannot transfer to your own wallet");
        }

        long amountInKobo = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        if (senderWallet.getBalance() < amountInKobo) {
            throw new BadRequestException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance() - amountInKobo);
        recipientWallet.setBalance(recipientWallet.getBalance() + amountInKobo);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        String reference = "TRF_" + System.currentTimeMillis();

        Transaction debitTransaction = Transaction.builder()
                .wallet(senderWallet)
                .type(TransactionType.TRANSFER)
                .amount(amountInKobo)
                .status(TransactionStatus.SUCCESS)
                .reference(reference + "_DEBIT")
                .metadata(Map.of("recipientWallet", recipientWallet.getWalletNumber()))
                .build();

        Transaction creditTransaction = Transaction.builder()
                .wallet(recipientWallet)
                .type(TransactionType.TRANSFER)
                .amount(amountInKobo)
                .status(TransactionStatus.SUCCESS)
                .reference(reference + "_CREDIT")
                .metadata(Map.of("senderWallet", senderWallet.getWalletNumber()))
                .build();

        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);

        log.info("Transfer completed: {} -> {}, amount: {} kobo",
                senderWallet.getWalletNumber(), recipientWallet.getWalletNumber(), amountInKobo);

        TransferResponse transferResponse = TransferResponse.builder()
                .status("success")
                .message("Transfer completed")
                .build();

        return ApiResponse.<TransferResponse>builder()
                .status("success")
                .statusCode(200)
                .message("Transfer completed successfully")
                .data(transferResponse)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<WithdrawResponse> withdraw(WithdrawRequest request) {
        User currentUser = getCurrentUser();
        Wallet wallet = walletRepository.findByUserIdForUpdate(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        long amountInKobo = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        if (wallet.getBalance() < amountInKobo) {
            throw new BadRequestException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance() - amountInKobo);
        walletRepository.save(wallet);

        String reference = "WDR_" + System.currentTimeMillis();

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.WITHDRAWAL)
                .amount(amountInKobo)
                .status(TransactionStatus.SUCCESS)
                .reference(reference)
                .metadata(new HashMap<>())
                .build();

        transactionRepository.save(transaction);

        log.info("Withdrawal completed: wallet {}, amount: {} kobo",
                wallet.getWalletNumber(), amountInKobo);

        WithdrawResponse withdrawResponse = WithdrawResponse.builder()
                .status("success")
                .message("Withdrawal completed")
                .build();

        return ApiResponse.<WithdrawResponse>builder()
                .status("success")
                .statusCode(200)
                .message("Withdrawal completed successfully")
                .data(withdrawResponse)
                .build();
    }

    @Override
    public ApiResponse<Page<TransactionResponse>> getTransactions(Pageable pageable) {
        User currentUser = getCurrentUser();
        Wallet wallet = getWalletByUser(currentUser);

        Page<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);

        Page<TransactionResponse> transactionResponses = transactions.map(t -> TransactionResponse.builder()
                .type(t.getType().name().toLowerCase())
                .amount(t.getAmountInNaira())
                .status(t.getStatus().name().toLowerCase())
                .reference(t.getReference())
                .createdAt(t.getCreatedAt())
                .build());

        return ApiResponse.<Page<TransactionResponse>>builder()
                .status("success")
                .statusCode(200)
                .message("Transactions retrieved successfully")
                .data(transactionResponses)
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Wallet getWalletByUser(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }
}