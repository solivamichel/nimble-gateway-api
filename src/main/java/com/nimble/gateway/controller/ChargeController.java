package com.nimble.gateway.controller;

import com.nimble.gateway.dto.request.ChargeRequest;
import com.nimble.gateway.dto.response.ChargeResponse;
import com.nimble.gateway.enums.ChargeStatus;
import com.nimble.gateway.service.ChargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/charges")
@Tag(name = "Cobranças", description = "Criação e consulta de cobranças entre usuários")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    @PostMapping
    @Operation(
            summary = "Criar nova cobrança",
            description = """
                    Cria uma cobrança onde o **usuário autenticado** é o originador, e o destinatário é identificado pelo CPF informado.
                    <br><br>
                    **Campos obrigatórios:**<br>
                    - `recipientCpf` — CPF do destinatário (usuário existente)<br>
                    - `amount` — Valor da cobrança<br>
                    - `description` — (opcional)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ChargeRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Criar cobrança",
                                            value = """
                                                    {
                                                      "recipientCpf": "91236796039",
                                                      "amount": 150.00,
                                                      "description": "Serviço de consultoria"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cobrança criada com sucesso",
                            content = @Content(schema = @Schema(implementation = ChargeResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Destinatário não encontrado", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Erro de validação", content = @Content)
            }
    )
    public ResponseEntity<ChargeResponse> create(
            @Valid @RequestBody ChargeRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(chargeService.create(principal.getName(), request));
    }

    @GetMapping("/sent")
    @Operation(
            summary = "Listar cobranças enviadas (originador)",
            description = """
                    Retorna todas as cobranças criadas pelo **usuário autenticado**, filtradas por status.<br><br>
                    **Status possíveis:**<br>
                    - `PENDING`: Ainda não pagas<br>
                    - `PAID`: Pagas<br>
                    - `CANCELED`: Canceladas
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de cobranças enviadas retornada com sucesso",
                            content = @Content(schema = @Schema(implementation = ChargeResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    public ResponseEntity<List<ChargeResponse>> sent(
            @RequestParam ChargeStatus status,
            Principal principal
    ) {
        return ResponseEntity.ok(chargeService.listSent(principal.getName(), status));
    }

    @GetMapping("/received")
    @Operation(
            summary = "Listar cobranças recebidas (destinatário)",
            description = """
                    Retorna todas as cobranças que o **usuário autenticado recebeu**, filtradas por status.<br><br>
                    **Status possíveis:**<br>
                    - `PENDING`: Ainda não pagas<br>
                    - `PAID`: Pagas<br>
                    - `CANCELED`: Canceladas
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de cobranças recebidas retornada com sucesso",
                            content = @Content(schema = @Schema(implementation = ChargeResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    public ResponseEntity<List<ChargeResponse>> received(
            @RequestParam ChargeStatus status,
            Principal principal
    ) {
        return ResponseEntity.ok(chargeService.listReceived(principal.getName(), status));
    }
}