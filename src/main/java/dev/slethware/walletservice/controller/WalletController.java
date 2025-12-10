package dev.slethware.walletservice.controller;

import dev.slethware.walletservice.models.dtos.request.DepositRequest;
import dev.slethware.walletservice.models.dtos.request.TransferRequest;
import dev.slethware.walletservice.models.dtos.request.WithdrawRequest;
import dev.slethware.walletservice.models.dtos.response.*;
import dev.slethware.walletservice.service.wallet.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "Endpoints for wallet operations")
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/deposit")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "apiKey")
    @PreAuthorize("hasAuthority('PERMISSION_DEPOSIT')")
    @Operation(
            summary = "Initiate Deposit",
            description = "Initialize a Paystack deposit transaction. Requires JWT or API key with 'deposit' permission."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deposit initiated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission denied")
    })
    public ResponseEntity<ApiResponse<DepositResponse>> initiateDeposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(walletService.initiateDeposit(request));
    }

    @PostMapping("/paystack/webhook")
    @Operation(
            summary = "Paystack Webhook",
            description = "Receive and process Paystack webhook events. Automatically credits wallet on successful payment."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid signature")
    })
    public ResponseEntity<Map<String, Boolean>> paystackWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {

        log.info("Received Paystack webhook");
        walletService.processWebhook(payload, signature);
        return ResponseEntity.ok(Map.of("status", true));
    }

    @GetMapping("/transaction/{reference}/status")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "apiKey")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @Operation(
            summary = "Get Transaction Status",
            description = "Check the status of any transaction (Deposit, Transfer, Withdrawal). Requires JWT or API key with 'read' permission." // Updated Description
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<ApiResponse<DepositStatusResponse>> getTransactionStatus(
            @Parameter(description = "Transaction reference") @PathVariable String reference) {
        return ResponseEntity.ok(walletService.getDepositStatus(reference));
    }

    @GetMapping("/balance")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "apiKey")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @Operation(
            summary = "Get Wallet Balance",
            description = "Retrieve current wallet balance. Requires JWT or API key with 'read' permission."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance() {
        return ResponseEntity.ok(walletService.getBalance());
    }

    @PostMapping("/transfer")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "apiKey")
    @PreAuthorize("hasAuthority('PERMISSION_TRANSFER')")
    @Operation(
            summary = "Transfer Funds",
            description = "Transfer funds to another wallet. Requires JWT or API key with 'transfer' permission."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recipient wallet not found")
    })
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(walletService.transfer(request));
    }

    @PostMapping("/withdraw")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('PERMISSION_WITHDRAW')")
    @Operation(
            summary = "Withdraw Funds",
            description = "Withdraw funds from wallet. Only accessible via JWT (no API key access). Minimum withdrawal is 50 NGN."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Withdrawal completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "API keys cannot access withdrawal endpoint"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<ApiResponse<WithdrawResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(walletService.withdraw(request));
    }

    @GetMapping("/transactions")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "apiKey")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @Operation(
            summary = "Get Transaction History",
            description = "Retrieve paginated transaction history for the current user's wallet. Requires JWT or API key with 'read' permission."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(walletService.getTransactions(pageable));
    }
}