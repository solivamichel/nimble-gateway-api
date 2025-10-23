package com.nimble.gateway.service;

import com.nimble.gateway.dto.request.UserRequest;
import com.nimble.gateway.entity.User;
import com.nimble.gateway.exception.BusinessException;
import com.nimble.gateway.repository.UserRepository;
import com.nimble.gateway.security.JwtTokenUtil;
import com.nimble.gateway.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest extends BaseServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenUtil jwtTokenUtil;

    @InjectMocks UserServiceImpl service;

    @DisplayName("Deve registrar um novo usuário com sucesso")
    @Test
    void register_ok() {
        UserRequest req = new UserRequest();
        req.setName("Maria");
        req.setCpf("44014614018");
        req.setEmail("maria@example.com");
        req.setPassword("Secr3t@123");

        when(userRepository.findByCpf("44014614018")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secr3t@123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(a -> a.getArgument(0));

        var resp = service.register(req);

        assertThat(resp.getCpf()).isEqualTo("44014614018");
        verify(userRepository).save(any(User.class));
    }

    @DisplayName("Deve lançar exceção ao tentar registrar usuário já existente (CPF duplicado)")
    @Test
    void register_duplicado() {
        when(userRepository.findByCpf("44014614018")).thenReturn(Optional.of(new User()));
        UserRequest req = new UserRequest();
        req.setName("Maria"); req.setCpf("44014614018"); req.setEmail("maria@example.com"); req.setPassword("x");
        assertThatThrownBy(() -> service.register(req)).isInstanceOf(BusinessException.class);
    }

    @DisplayName("Deve realizar login com e-mail e retornar token JWT válido")
    @Test
    void login_por_email() {
        User u = User.builder().id(1L).cpf("44014614018").email("maria@example.com").passwordHash("hash").build();
        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Secr3t@123","hash")).thenReturn(true);
        when(jwtTokenUtil.generateToken("44014614018")).thenReturn("jwt");
        String token = service.login("maria@example.com","Secr3t@123");
        assertThat(token).isEqualTo("jwt");
    }
}