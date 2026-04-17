package com.MMCBank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
    @NotNull Long fromAccountId,

     @NotNull DestinationType destinationType,

     Long toAccountId,
     String toAccountNumber,
    @NotNull @Positive BigDecimal amount,
    String description
) {}
