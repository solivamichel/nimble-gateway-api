package com.nimble.gateway.entity;

import com.nimble.gateway.enums.ChargeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "charges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Charge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User originator;

    @ManyToOne(optional = false)
    private User recipient;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeStatus status = ChargeStatus.PENDING;

    private OffsetDateTime paidAt;
}
