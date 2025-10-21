package com.nimble.gateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(name = "DepositRequest", description = "Payload para depósito de saldo")
@Data
public class DepositRequest {

    @NotNull
    @DecimalMin("0.01")
    @Schema(example = "500.00", minimum = "0.01")
    private BigDecimal amount;
}