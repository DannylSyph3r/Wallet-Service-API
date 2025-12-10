package dev.slethware.walletservice.service.wallet;

import dev.slethware.walletservice.models.dtos.request.DepositRequest;
import dev.slethware.walletservice.models.dtos.request.TransferRequest;
import dev.slethware.walletservice.models.dtos.request.WithdrawRequest;
import dev.slethware.walletservice.models.dtos.response.*;
import dev.slethware.walletservice.models.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface WalletService {
    void createWalletForUser(User user);
    ApiResponse<DepositResponse> initiateDeposit(DepositRequest request);
    void processWebhook(Map<String, Object> payload, String signature);
    ApiResponse<DepositStatusResponse> getDepositStatus(String reference);
    ApiResponse<BalanceResponse> getBalance();
    ApiResponse<TransferResponse> transfer(TransferRequest request);
    ApiResponse<WithdrawResponse> withdraw(WithdrawRequest request);
    ApiResponse<Page<TransactionResponse>> getTransactions(Pageable pageable);
}