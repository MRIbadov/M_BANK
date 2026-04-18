package com.MMCBank;

import com.MMCBank.entity.Account;
import com.MMCBank.entity.Transaction;
import com.MMCBank.entity.User;
import com.MMCBank.repository.AccountRepository;
import com.MMCBank.repository.TransactionRepository;
import com.MMCBank.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@SpringBootApplication
public class MMCBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MMCBankApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(
            UserRepository userRepo,
            AccountRepository accountRepo,
            TransactionRepository txRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {
            User user = new User();
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setEmail("john.doe@example.com");
            user.setUsername("john.doe");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setPhoneNumber("+48 123 456 789");
            user = userRepo.save(user);

            Account checking = new Account();
            checking.setAccountNumber("PL10 1050 0099 7603 1234 5678 9012");
            checking.setAccountNumberHash(hashAccountNumber(checking.getAccountNumber()));
            checking.setAccountType(Account.AccountType.CHECKING);
            checking.setAccountName("Main Checking");
            checking.setBalance(new BigDecimal("18450.00"));
            checking.setCurrency("PLN");
            checking.setUser(user);
            checking = accountRepo.save(checking);

            Account savings = new Account();
            savings.setAccountNumber("PL10 1050 0099 7603 9876 5432 1098");
            savings.setAccountNumberHash(hashAccountNumber(savings.getAccountNumber()));
            savings.setAccountType(Account.AccountType.SAVINGS);
            savings.setAccountName("Savings Reserve");
            savings.setBalance(new BigDecimal("54200.00"));
            savings.setCurrency("PLN");
            savings.setUser(user);
            savings = accountRepo.save(savings);

            createTx(txRepo, "Salary — March", new BigDecimal("8500.00"),
                    Transaction.TransactionType.CREDIT, checking, null, LocalDateTime.now().minusDays(1));
            createTx(txRepo, "Grocery Store", new BigDecimal("245.60"),
                    Transaction.TransactionType.DEBIT, checking, null, LocalDateTime.now().minusDays(2));
            createTx(txRepo, "Monthly savings transfer", new BigDecimal("2000.00"),
                    Transaction.TransactionType.TRANSFER, checking, savings, LocalDateTime.now().minusDays(3));
            createTx(txRepo, "Electricity bill", new BigDecimal("380.00"),
                    Transaction.TransactionType.DEBIT, checking, null, LocalDateTime.now().minusDays(5));
            createTx(txRepo, "Online transfer from friend", new BigDecimal("500.00"),
                    Transaction.TransactionType.CREDIT, savings, null, LocalDateTime.now().minusDays(7));

            System.out.println("\n======================================");
            System.out.println("  SKY BANK started successfully!");
            System.out.println("  URL:      http://localhost:8080");
            System.out.println("  H2 Console: http://localhost:8080/h2-console");
            System.out.println("======================================\n");
        };
    }

    private void createTx(TransactionRepository repo, String desc, BigDecimal amount,
                           Transaction.TransactionType type, Account from, Account to, LocalDateTime date) {
        Transaction tx = new Transaction();
        tx.setDescription(desc);
        tx.setAmount(amount);
        tx.setTransactionType(type);
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setCreatedAt(date);
        tx.setStatus(Transaction.TransactionStatus.COMPLETED);
        tx.setReference("TXN" + System.nanoTime());
        repo.save(tx);
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
