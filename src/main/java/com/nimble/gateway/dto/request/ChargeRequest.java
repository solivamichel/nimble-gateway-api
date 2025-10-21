package com.nimble.gateway.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargeRequest {

    @NotBlank
    private String recipientCpf;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String description;
}
