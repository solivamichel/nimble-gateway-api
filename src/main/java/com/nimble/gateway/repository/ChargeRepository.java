package com.nimble.gateway.repository;

import com.nimble.gateway.entity.Charge;
import com.nimble.gateway.entity.User;
import com.nimble.gateway.enums.ChargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findByOriginatorAndStatus(User originator, ChargeStatus status);

    List<Charge> findByRecipientAndStatus(User recipient, ChargeStatus status);
}
