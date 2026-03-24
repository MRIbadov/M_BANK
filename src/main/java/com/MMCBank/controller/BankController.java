package com.MMCBank.controller;

import com.MMCBank.dto.*;
import com.MMCBank.entity.Account;
import com.MMCBank.entity.User;
import com.MMCBank.service.BankService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    // GET /api/bank/profile
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserProfileResponse(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getUsername(), user.getPhoneNumber()));
    }

    // GET /api/bank/accounts
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAccounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bankService.getAccounts(user));
    }

    // POST /api/bank/accounts
    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "New Account");
        String type = body.getOrDefault("type", "CHECKING");
        Account.AccountType accountType;
        try {
            accountType = Account.AccountType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            accountType = Account.AccountType.CHECKING;
        }
        return ResponseEntity.ok(bankService.createAccount(user, name, accountType));
    }

    // GET /api/bank/transactions
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bankService.getAllTransactions(user));
    }

    // GET /api/bank/accounts/{id}/transactions
    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(
            @AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(bankService.getAccountTransactions(user, id));
    }

    // POST /api/bank/transfer
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest req) {
        try {
            return ResponseEntity.ok(bankService.transfer(user, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
