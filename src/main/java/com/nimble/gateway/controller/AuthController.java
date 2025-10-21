package com.nimble.gateway.controller;

import com.nimble.gateway.dto.request.LoginRequest;
import com.nimble.gateway.dto.request.UserRequest;
import com.nimble.gateway.dto.response.AuthResponse;
import com.nimble.gateway.dto.response.UserResponse;
import com.nimble.gateway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Autenticação", description = "Cadastro e login de usuários (público)")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria um novo usuário com nome, CPF, e-mail e senha. Valida CPF e campos obrigatórios.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Cadastro OK",
                                            value = """
                                                    {
                                                      "name": "Michel",
                                                      "cpf": "39053344705",
                                                      "email": "michel@example.com",
                                                      "password": "Secr3t@123"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário criado",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Erros de validação de campos", content = @Content),
                    @ApiResponse(responseCode = "422", description = "CPF/E-mail já cadastrados ou CPF inválido", content = @Content)
            }
    )
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login do usuário",
            description = """
                    Autentica por **CPF** ou **e-mail** no campo `username` e retorna um **JWT**.
                    Ex.: { "username": "39053344705", "password": "Secr3t@123" } ou
                         { "username": "michel@example.com", "password": "Secr3t@123" }.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Login por CPF",
                                            value = """
                                                    { "username": "39053344705", "password": "Secr3t@123" }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Login por e-mail",
                                            value = """
                                                    { "username": "michel@example.com", "password": "Secr3t@123" }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autenticado",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Erros de validação de campos", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Credenciais inválidas", content = @Content)
            }
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
