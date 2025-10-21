package com.nimble.gateway.controller;

import com.nimble.gateway.dto.request.ChargeRequest;
import com.nimble.gateway.dto.response.ChargeResponse;
import com.nimble.gateway.enums.ChargeStatus;
import com.nimble.gateway.service.ChargeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/charges")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    // Criar cobrança (autenticado) originador = CPF do token
    @PostMapping
    public ResponseEntity<ChargeResponse> create(@Valid @RequestBody ChargeRequest request,
                                                 Principal principal) {
        return ResponseEntity.ok(chargeService.create(principal.getName(), request));
    }

    // Cobranças que eu criei (originador)
    @GetMapping("/sent")
    public ResponseEntity<List<ChargeResponse>> sent(@RequestParam ChargeStatus status,
                                                     Principal principal) {
        return ResponseEntity.ok(chargeService.listSent(principal.getName(), status));
    }

    // Cobranças que eu recebi (destinatário)
    @GetMapping("/received")
    public ResponseEntity<List<ChargeResponse>> received(@RequestParam ChargeStatus status,
                                                         Principal principal) {
        return ResponseEntity.ok(chargeService.listReceived(principal.getName(), status));
    }
}

