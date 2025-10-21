package com.nimble.gateway.controller;

import com.nimble.gateway.dto.request.DepositRequest;
import com.nimble.gateway.dto.request.PaymentRequest;
import com.nimble.gateway.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Pagamentos", description = "Endpoints de depósito e pagamento de cobranças")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/deposit")
    @Operation(
            summary = "Realiza depósito de saldo",
            description = "Efetua depósito na conta do usuário autenticado após validação com o autorizador externo.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DepositRequest.class),
                            examples = @ExampleObject(
                                    name = "Depósito OK",
                                    value = """
                                            { "amount": 500.00 }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Saldo atualizado retornado no corpo"),
                    @ApiResponse(responseCode = "401", description = "Não autenticado"),
                    @ApiResponse(responseCode = "422", description = "Depósito não autorizado pelo autorizador externo")
            }
    )
    public ResponseEntity<BigDecimal> deposit(
            @Valid @org.springframework.web.bind.annotation.RequestBody DepositRequest request,
            Principal principal
    ) {
        BigDecimal newBalance = paymentService.deposit(principal.getName(), request);
        return ResponseEntity.ok(newBalance);
    }

    @PostMapping("/pay")
    @Operation(
            summary = "Pagar cobrança",
            description = """
                    Paga uma cobrança PENDING:
                    - **BALANCE**: verifica saldo do destinatário, debita pagador e credita originador.
                    - **CARD**: consulta autorizador externo; se aprovado, credita originador (não mexe no saldo do pagador).
                    Somente o destinatário da cobrança pode pagar.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PaymentRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Pagar com saldo",
                                            value = """
                                                    { "chargeId": 2, "method": "BALANCE" }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Pagar com cartão",
                                            value = """
                                                    {
                                                      "chargeId": 4,
                                                      "method": "CARD",
                                                      "cardNumber": "4111111111111111",
                                                      "cardExpiration": "12/29",
                                                      "cardCvv": "123"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pagamento realizado"),
                    @ApiResponse(responseCode = "401", description = "Não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Cobrança/usuário não encontrado"),
                    @ApiResponse(responseCode = "422", description = "Regra de negócio (saldo insuficiente, não é o destinatário, cobrança não pendente, cartão não autorizado, etc.)")
            }
    )
    public ResponseEntity<Void> pay(
            @Valid @org.springframework.web.bind.annotation.RequestBody PaymentRequest request,
            Principal principal
    ) {
        paymentService.pay(principal.getName(), request);
        return ResponseEntity.ok().build();
    }
}