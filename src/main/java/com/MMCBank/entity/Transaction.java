package com.MMCBank.entity;

import com.MMCBank.security.EncryptionConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    public enum TransactionType { CREDIT, DEBIT, TRANSFER }
    public enum TransactionStatus { PENDING, COMPLETED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique reference number
    @Column(nullable = false, unique = true)
    private String reference;

    // Description is AES-encrypted at rest
    @Convert(converter = EncryptionConverter.class)
    @Column(length = 1024)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
