package com.nimble.gateway.service.impl;

import com.nimble.gateway.dto.request.ChargeRequest;
import com.nimble.gateway.dto.response.ChargeResponse;
import com.nimble.gateway.entity.Charge;
import com.nimble.gateway.entity.User;
import com.nimble.gateway.enums.ChargeStatus;
import com.nimble.gateway.exception.BusinessException;
import com.nimble.gateway.exception.NotFoundException;
import com.nimble.gateway.repository.ChargeRepository;
import com.nimble.gateway.repository.UserRepository;
import com.nimble.gateway.service.AuthorizerClient;
import com.nimble.gateway.service.ChargeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.nimble.gateway.enums.PaymentMethod;

@Service
public class ChargeServiceImpl implements ChargeService {

    private final ChargeRepository chargeRepository;
    private final UserRepository userRepository;
    private final AuthorizerClient authorizer;

    public ChargeServiceImpl(ChargeRepository chargeRepository, UserRepository userRepository, AuthorizerClient authorizer) {
        this.chargeRepository = chargeRepository;
        this.userRepository = userRepository;
        this.authorizer = authorizer;
    }

    @Override
    public ChargeResponse create(String originatorCpf, ChargeRequest request) {
        User originator = userRepository.findByCpf(originatorCpf)
                .orElseThrow(() -> new NotFoundException("Originador não encontrado"));

        User recipient = userRepository.findByCpf(request.getRecipientCpf())
                .orElseThrow(() -> new NotFoundException("Destinatário não encontrado"));

        if (originator.getCpf().equals(recipient.getCpf())) {
            throw new BusinessException("Não é permitido criar cobrança para si mesmo");
        }

        Charge c = Charge.builder()
                .originator(originator)
                .recipient(recipient)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(ChargeStatus.PENDING)
                .build();

        c = chargeRepository.save(c);
        return toResponse(c);
    }

    @Override
    public List<ChargeResponse> listSent(String originatorCpf, ChargeStatus status) {
        User originator = userRepository.findByCpf(originatorCpf)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return chargeRepository.findByOriginatorAndStatus(originator, status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ChargeResponse> listReceived(String recipientCpf, ChargeStatus status) {
        User recipient = userRepository.findByCpf(recipientCpf)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return chargeRepository.findByRecipientAndStatus(recipient, status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ChargeResponse toResponse(Charge c) {
        return ChargeResponse.builder()
                .id(c.getId())
                .originatorCpf(c.getOriginator().getCpf())
                .recipientCpf(c.getRecipient().getCpf())
                .amount(c.getAmount())
                .description(c.getDescription())
                .status(c.getStatus())
                .paidAt(c.getPaidAt())
                .build();
    }

    @Override
    public ChargeResponse cancel(Long chargeId, String requesterCpf) {
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new NotFoundException("Cobrança não encontrada"));

        // só o originador pode cancelar
        if (!charge.getOriginator().getCpf().equals(requesterCpf)) {
            throw new BusinessException("Apenas o originador pode cancelar esta cobrança");
        }

        if (charge.getStatus() == ChargeStatus.CANCELLED) {
            throw new BusinessException("Cobrança já está cancelada");
        }

        if (charge.getStatus() == ChargeStatus.PENDING) {
            charge.setStatus(ChargeStatus.CANCELLED);
            chargeRepository.save(charge);
            return toResponse(charge);
        }

        if (charge.getStatus() == ChargeStatus.PAID) {

            // precisa ter registro do método de pagamento
            if (charge.getPaymentMethod() == null) {
                throw new BusinessException("Método de pagamento desconhecido para cancelamento");
            }

            User originator = userRepository.findById(charge.getOriginator().getId())
                    .orElseThrow(() -> new NotFoundException("Originador não encontrado"));

            // quem pagou foi o destinatário
            User payer = userRepository.findByCpf(charge.getPaidByCpf())
                    .orElseThrow(() -> new NotFoundException("Pagador não encontrado"));

            if (charge.getPaymentMethod() == PaymentMethod.BALANCE) {
                // estorno: tirar do originador e devolver ao pagador
                if (originator.getBalance().compareTo(charge.getAmount()) < 0) {
                    throw new BusinessException("Saldo do originador insuficiente para estorno");
                }
                originator.setBalance(originator.getBalance().subtract(charge.getAmount()));
                payer.setBalance(payer.getBalance().add(charge.getAmount()));
                userRepository.save(originator);
                userRepository.save(payer);

            } else if (charge.getPaymentMethod() == PaymentMethod.CARD) {
                // consulta autorizador externo
                if (!authorizer.isApproved()) {
                    throw new BusinessException("Autorizador externo recusou o cancelamento");
                }
                // reverte crédito concedido ao originador na liquidação do cartão
                if (originator.getBalance().compareTo(charge.getAmount()) < 0) {
                    throw new BusinessException("Saldo do originador insuficiente para reversão");
                }
                originator.setBalance(originator.getBalance().subtract(charge.getAmount()));
                userRepository.save(originator);
            }

            charge.setStatus(ChargeStatus.CANCELLED);
            chargeRepository.save(charge);
            return toResponse(charge);
        }

        throw new BusinessException("Estado da cobrança inválido para cancelamento");
    }
}