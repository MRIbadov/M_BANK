package com.MMCBank.dto;
import java.math.BigDecimal;
public record AccountResponse(
    Long id, String accountNumber, String accountName,
    String accountType, BigDecimal balance, String currency
) {}
