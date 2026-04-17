package com.MMCBank.service;

import com.MMCBank.dto.*;
import com.MMCBank.entity.Account;
import com.MMCBank.entity.Transaction;
import com.MMCBank.entity.User;
import com.MMCBank.repository.AccountRepository;
import com.MMCBank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BankService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;

    public BankService(AccountRepository accountRepo, TransactionRepository txRepo) {
        this.accountRepo = accountRepo;
        this.txRepo      = txRepo;
    }

    public List<AccountResponse> getAccounts(User user) {
        return accountRepo.findByUserAndActiveTrue(user)
                .stream().map(this::toAccountResponse).toList();
    }

    public AccountResponse createAccount(User user, String name, Account.AccountType type) {
        Account acc = new Account();
        String iban = generateUniqueIban();
        acc.setAccountName(name);
        acc.setAccountType(type);
        acc.setAccountNumber(iban);
        acc.setAccountNumberHash(hashAccountNumber(iban));
        acc.setCurrency("PLN");
        acc.setUser(user);
        return toAccountResponse(accountRepo.save(acc));
    }


    public List<TransactionResponse> getAllTransactions(User user) {
        List<Account> accounts = accountRepo.findByUserAndActiveTrue(user);
        if (accounts.isEmpty()) {
            return List.of();
        }
        return txRepo.findByAccounts(accounts)
                .stream().map(this::toTxResponse).toList();
    }

    public List<TransactionResponse> getAccountTransactions(User user, Long accountId) {
        Account acc = accountRepo.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return txRepo.findByAccount(acc)
                .stream().map(this::toTxResponse).toList();
    }



    @Transactional
    public TransactionResponse transfer(User user, TransferRequest req) {
        Account from = accountRepo.findByIdAndUser(req.fromAccountId(), user)
                .orElseThrow(() -> new RuntimeException("Source account not found or not yours"));

        Account to;

        if (req.destinationType() == DestinationType.DOMESTIC) {
            if (req.toAccountId() == null) {
                throw new IllegalArgumentException("Destination account is required");
            }

            if (req.fromAccountId().equals(req.toAccountId())) {
                throw new IllegalArgumentException("Cannot transfer to the same account");
            }

            to = accountRepo.findById(req.toAccountId())
                    .orElseThrow(() -> new RuntimeException("Destination account not found"));
        } else {
            if (req.toAccountNumber() == null || req.toAccountNumber().isBlank()) {
                throw new IllegalArgumentException("Destination IBAN is required");
            }

            to = accountRepo.findByAccountNumberHash(hashAccountNumber(req.toAccountNumber()))
                    .orElseThrow(() -> new RuntimeException("Destination account not found"));

            if (from.getId().equals(to.getId())) {
                throw new IllegalArgumentException("Cannot transfer to the same account");
            }
        }

        if (from.getBalance().compareTo(req.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(req.amount()));
        to.setBalance(to.getBalance().add(req.amount()));
        accountRepo.save(from);
        accountRepo.save(to);

        Transaction tx = new Transaction();
        tx.setReference("TXN" + System.nanoTime());
        tx.setDescription(req.description() != null ? req.description() : "Transfer");
        tx.setAmount(req.amount());
        tx.setTransactionType(Transaction.TransactionType.TRANSFER);
        tx.setStatus(Transaction.TransactionStatus.COMPLETED);
        tx.setFromAccount(from);
        tx.setToAccount(to);

        return toTxResponse(txRepo.save(tx));
    }


    private AccountResponse toAccountResponse(Account a) {
        return new AccountResponse(
                a.getId(), a.getAccountNumber(), a.getAccountName(),
                a.getAccountType().name(), a.getBalance(), a.getCurrency());
    }

    private TransactionResponse toTxResponse(Transaction t) {
        String from = t.getFromAccount() != null ? t.getFromAccount().getAccountName() : "External";
        String to   = t.getToAccount()   != null ? t.getToAccount().getAccountName()   : "External";
        return new TransactionResponse(
                t.getId(), t.getReference(), t.getDescription(),
                t.getAmount(), t.getTransactionType().name(), t.getStatus().name(),
                from, to, t.getCreatedAt().format(FMT));
    }

    private String generateIban() {
            return String.format("PL%02d %04d %04d %04d %04d %04d %04d",
                    (int)(Math.random()*90+10),
                    (int)(Math.random()*9000+1000), (int)(Math.random()*9000+1000),
                    (int)(Math.random()*9000+1000), (int)(Math.random()*9000+1000),
                    (int)(Math.random()*9000+1000), (int)(Math.random()*9000+1000));
    }

    private String generateUniqueIban() {
        String uniqueIban;
        do {
            uniqueIban = generateIban();
        } while (accountRepo.existsByAccountNumberHash(hashAccountNumber(uniqueIban)));

        return uniqueIban;
    }

    private String hashAccountNumber(String accountNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizeAccountNumber(accountNumber).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String normalizeAccountNumber(String accountNumber) {
        return accountNumber.replaceAll("\\s+", "").toUpperCase();
    }
}
