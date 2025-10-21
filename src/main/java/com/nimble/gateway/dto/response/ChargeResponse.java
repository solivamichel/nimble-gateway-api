package com.nimble.gateway.dto.response;

import com.nimble.gateway.enums.ChargeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data @Builder
public class ChargeResponse {

    private Long id;
    private String originatorCpf;
    private String recipientCpf;
    private BigDecimal amount;
    private String description;
    private ChargeStatus status;
    private OffsetDateTime paidAt;
}