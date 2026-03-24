package com.MMCBank.dto;
import java.math.BigDecimal;

public record TransactionResponse(
    Long id, String reference, String description, BigDecimal amount,
    String transactionType, String status,
    String fromAccount, String toAccount, String createdAt
) {}
