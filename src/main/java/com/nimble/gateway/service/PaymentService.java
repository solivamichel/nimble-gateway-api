package com.nimble.gateway.service;

import com.nimble.gateway.dto.request.DepositRequest;
import com.nimble.gateway.dto.request.PaymentRequest;

import java.math.BigDecimal;

public interface PaymentService {

    BigDecimal deposit(String cpf, DepositRequest request);

    void pay(String payerCpf, PaymentRequest request);
}