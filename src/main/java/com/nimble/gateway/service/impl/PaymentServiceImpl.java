package com.nimble.gateway.service.impl;

import com.nimble.gateway.dto.request.DepositRequest;
import com.nimble.gateway.dto.request.PaymentRequest;
import com.nimble.gateway.entity.Charge;
import com.nimble.gateway.entity.User;
import com.nimble.gateway.enums.ChargeStatus;
import com.nimble.gateway.enums.PaymentMethod;
import com.nimble.gateway.exception.BusinessException;
import com.nimble.gateway.exception.NotFoundException;
import com.nimble.gateway.repository.ChargeRepository;
import com.nimble.gateway.repository.UserRepository;
import com.nimble.gateway.service.AuthorizerClient;
import com.nimble.gateway.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final ChargeRepository chargeRepository;
    private final AuthorizerClient authorizer;

    public PaymentServiceImpl(UserRepository userRepository, AuthorizerClient authorizer,
                              ChargeRepository chargeRepository) {
        this.userRepository = userRepository;
        this.authorizer = authorizer;
        this.chargeRepository = chargeRepository;
    }

    @Override
    public BigDecimal deposit(String cpf, DepositRequest request) {
        if (!authorizer.isApproved()) throw new BusinessException("Depósito não autorizado pelo autorizador externo");

        User user = userRepository.findByCpf(cpf)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        user.setBalance(user.getBalance().add(request.getAmount()));
        userRepository.save(user);
        return user.getBalance();
    }

    @Override
    public void pay(String payerCpf, PaymentRequest request) {
        Charge charge = chargeRepository.findById(request.getChargeId())
                .orElseThrow(() -> new NotFoundException("Cobrança não encontrada"));

        if (charge.getStatus() != ChargeStatus.PENDING)
            throw new BusinessException("Cobrança não está pendente");

        // regra: quem paga é o destinatário
        if (!charge.getRecipient().getCpf().equals(payerCpf))
            throw new BusinessException("Apenas o destinatário pode pagar esta cobrança");

        User payer = userRepository.findByCpf(payerCpf)
                .orElseThrow(() -> new NotFoundException("Pagador não encontrado"));
        User originator = userRepository.findByCpf(charge.getOriginator().getCpf())
                .orElseThrow(() -> new NotFoundException("Originador não encontrado"));

        if (request.getMethod() == PaymentMethod.BALANCE) {
            if (payer.getBalance().compareTo(charge.getAmount()) < 0)
                throw new BusinessException("Saldo insuficiente");
            payer.setBalance(payer.getBalance().subtract(charge.getAmount()));
            originator.setBalance(originator.getBalance().add(charge.getAmount()));
            userRepository.save(payer);
            userRepository.save(originator);

        } else if (request.getMethod() == PaymentMethod.CARD) {
            // checagem simples: precisa vir dados de cartão
            if (request.getCardNumber() == null || request.getCardExpiration() == null || request.getCardCvv() == null)
                throw new BusinessException("Dados de cartão incompletos");

            if (!authorizer.isApproved())
                throw new BusinessException("Pagamento via cartão não autorizado pelo autorizador externo");

            // credita diretamente o originador
            originator.setBalance(originator.getBalance().add(charge.getAmount()));
            userRepository.save(originator);
        } else {
            throw new BusinessException("Método de pagamento inválido");
        }

        charge.setStatus(ChargeStatus.PAID);
        charge.setPaidAt(OffsetDateTime.now());
        charge.setPaymentMethod(request.getMethod());
        charge.setPaidByCpf(payerCpf);
        chargeRepository.save(charge);
    }
}