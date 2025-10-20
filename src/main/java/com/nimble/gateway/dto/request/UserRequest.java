package com.nimble.gateway.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String cpf;

    @Email
    @NotBlank
    private String email;

    @NotBlank @Size(min=6, max=60)
    private String password;
}