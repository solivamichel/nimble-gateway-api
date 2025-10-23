package com.nimble.gateway.service;

import com.nimble.gateway.dto.request.ChargeRequest;
import com.nimble.gateway.entity.Charge;
import com.nimble.gateway.entity.User;
import com.nimble.gateway.enums.ChargeStatus;
import com.nimble.gateway.repository.ChargeRepository;
import com.nimble.gateway.repository.UserRepository;
import com.nimble.gateway.service.impl.ChargeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChargeServiceTest extends BaseServiceTest {

    @Mock ChargeRepository chargeRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ChargeServiceImpl service;

    @DisplayName("Deve criar cobrança pendente com sucesso")
    @Test
    void create_ok() {
        String originatorCpf = "39053344705";
        User originator = User.builder().id(1L).cpf(originatorCpf).build();
        User recipient = User.builder().id(2L).cpf("44014614018").build();

        when(userRepository.findByCpf(originatorCpf)).thenReturn(Optional.of(originator));
        when(userRepository.findByCpf("44014614018")).thenReturn(Optional.of(recipient));
        when(chargeRepository.save(any(Charge.class))).thenAnswer(a -> a.getArgument(0));

        ChargeRequest req = new ChargeRequest();
        req.setRecipientCpf("44014614018");
        req.setAmount(new BigDecimal("150.00"));
        req.setDescription("Serviço X");

        var resp = service.create(originatorCpf, req);

        assertThat(resp.getStatus()).isEqualTo(ChargeStatus.PENDING);
        assertThat(resp.getAmount()).isEqualTo(new BigDecimal("150.00"));
        verify(chargeRepository).save(any(Charge.class));
    }
}
