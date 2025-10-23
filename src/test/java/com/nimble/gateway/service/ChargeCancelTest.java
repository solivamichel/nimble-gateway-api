package com.nimble.gateway.service;

import com.nimble.gateway.entity.Charge;
import com.nimble.gateway.entity.User;
import com.nimble.gateway.enums.ChargeStatus;
import com.nimble.gateway.enums.PaymentMethod;
import com.nimble.gateway.exception.BusinessException;
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

class ChargeCancelTest extends BaseServiceTest {

    @Mock ChargeRepository chargeRepository;
    @Mock UserRepository userRepository;
    @Mock AuthorizerClient authorizerClient;

    @InjectMocks ChargeServiceImpl service;

    @DisplayName("Deve cancelar cobrança pendente com sucesso")
    @Test
    void cancel_pending_ok() {
        User originator = User.builder().id(1L).cpf("39053344705").build();
        User dest = User.builder().id(2L).cpf("44014614018").build();
        Charge c = Charge.builder().id(10L).originator(originator).recipient(dest)
                .amount(new BigDecimal("100.00")).status(ChargeStatus.PENDING).build();

        when(chargeRepository.findById(10L)).thenReturn(Optional.of(c));

        var resp = service.cancel(10L, "39053344705");
        assertThat(resp.getStatus()).isEqualTo(ChargeStatus.CANCELLED);
    }

    @DisplayName("Deve estornar valores ao cancelar cobrança paga com saldo")
    @Test
    void cancel_paid_balance_estorna() {
        User originator = User.builder().id(1L).cpf("39053344705").balance(new BigDecimal("200.00")).build();
        User payer = User.builder().id(2L).cpf("44014614018").balance(new BigDecimal("50.00")).build();
        Charge c = Charge.builder().id(10L).originator(originator).recipient(payer)
                .amount(new BigDecimal("100.00")).status(ChargeStatus.PAID)
                .paymentMethod(PaymentMethod.BALANCE).paidByCpf("44014614018").build();

        when(chargeRepository.findById(10L)).thenReturn(Optional.of(c));
        when(userRepository.findById(1L)).thenReturn(Optional.of(originator));
        when(userRepository.findByCpf("44014614018")).thenReturn(Optional.of(payer));

        var resp = service.cancel(10L, "39053344705");

        assertThat(resp.getStatus()).isEqualTo(ChargeStatus.CANCELLED);
        assertThat(originator.getBalance()).isEqualTo(new BigDecimal("100.00"));
        assertThat(payer.getBalance()).isEqualTo(new BigDecimal("150.00"));
    }

    @DisplayName("Deve reverter crédito ao cancelar cobrança paga com cartão")
    @Test
    void cancel_paid_card_reverteCredito() {
        User originator = User.builder().id(1L).cpf("39053344705").balance(new BigDecimal("150.00")).build();
        User payer = User.builder().id(2L).cpf("44014614018").build();
        Charge c = Charge.builder().id(10L).originator(originator).recipient(payer)
                .amount(new BigDecimal("150.00")).status(ChargeStatus.PAID)
                .paymentMethod(PaymentMethod.CARD).paidByCpf("44014614018").build();

        when(authorizerClient.isApproved()).thenReturn(true);
        when(chargeRepository.findById(10L)).thenReturn(Optional.of(c));
        when(userRepository.findById(1L)).thenReturn(Optional.of(originator));
        when(userRepository.findByCpf("44014614018")).thenReturn(Optional.of(payer));

        var resp = service.cancel(10L, "39053344705");

        assertThat(resp.getStatus()).isEqualTo(ChargeStatus.CANCELLED);
        assertThat(originator.getBalance()).isEqualByComparingTo("0.00");
    }

    @DisplayName("Deve impedir cancelamento por usuário que não é o originador da cobrança")
    @Test
    void cancel_apenas_originador() {
        User originator = User.builder().id(1L).cpf("39053344705").build();
        User dest = User.builder().id(2L).cpf("44014614018").build();
        Charge c = Charge.builder().id(10L).originator(originator).recipient(dest)
                .amount(new BigDecimal("100.00")).status(ChargeStatus.PENDING).build();

        when(chargeRepository.findById(10L)).thenReturn(Optional.of(c));
        assertThatThrownBy(() -> service.cancel(10L, "44014614018"))
                .isInstanceOf(BusinessException.class);
    }
}
