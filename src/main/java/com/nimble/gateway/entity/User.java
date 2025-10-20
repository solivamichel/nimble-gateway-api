package com.nimble.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false, unique=true)
    private String cpf;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String passwordHash;

    @Column(nullable=false)
    private BigDecimal balance = BigDecimal.ZERO;
}