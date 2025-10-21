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
import com.nimble.gateway.service.ChargeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargeServiceImpl implements ChargeService {

    private final ChargeRepository chargeRepository;
    private final UserRepository userRepository;

    public ChargeServiceImpl(ChargeRepository chargeRepository, UserRepository userRepository) {
        this.chargeRepository = chargeRepository;
        this.userRepository = userRepository;
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
}