package com.nimble.gateway.dto.request;

import com.nimble.gateway.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(name = "PaymentRequest", description = "Payload para pagamento de cobrança")
@Data
public class PaymentRequest {

    @Schema(example = "2")
    @NotNull
    private Long chargeId;

    @NotNull
    @Schema(example = "BALANCE", allowableValues = {"BALANCE","CARD"})
    private PaymentMethod method;

    @Schema(example = "4111111111111111")
    private String cardNumber;

    @Schema(example = "12/29")
    private String cardExpiration;

    @Schema(example = "123")
    private String cardCvv;
}