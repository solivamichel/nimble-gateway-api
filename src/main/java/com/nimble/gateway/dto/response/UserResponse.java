package com.nimble.gateway.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class UserResponse {

    private Long id;

    private String name;

    private String cpf;

    private String email;

    private BigDecimal balance;
}
