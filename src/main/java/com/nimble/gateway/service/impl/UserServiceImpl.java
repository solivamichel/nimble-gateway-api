package com.nimble.gateway.service.impl;

import com.nimble.gateway.dto.request.UserRequest;
import com.nimble.gateway.dto.response.UserResponse;
import com.nimble.gateway.entity.User;
import com.nimble.gateway.exception.BusinessException;
import com.nimble.gateway.repository.UserRepository;
import com.nimble.gateway.security.JwtTokenUtil;
import com.nimble.gateway.service.UserService;
import com.nimble.gateway.util.CpfValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwt;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenUtil jwt) {
        this.userRepository = userRepository; this.passwordEncoder = passwordEncoder; this.jwt = jwt;
    }

    @Override
    public UserResponse register(UserRequest req) {
        if (!CpfValidator.isValid(req.getCpf())) throw new BusinessException("CPF inválido");
        userRepository.findByCpf(req.getCpf()).ifPresent(u -> { throw new BusinessException("CPF já cadastrado"); });
        userRepository.findByEmail(req.getEmail()).ifPresent(u -> { throw new BusinessException("E-mail já cadastrado"); });

        User u = User.builder()
                .name(req.getName()).cpf(req.getCpf()).email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .balance(BigDecimal.ZERO).build();

        userRepository.save(u);
        return UserResponse.builder()
                .id(u.getId()).name(u.getName()).cpf(u.getCpf()).email(u.getEmail()).balance(u.getBalance()).build();
    }

    @Override
    public String login(String username, String password) {
        User u = getByCpfOrEmail(username);
        if (!passwordEncoder.matches(password, u.getPasswordHash()))
            throw new BusinessException("Credenciais inválidas");
        return jwt.generateToken(u.getCpf());
    }

    @Override
    public User getByCpfOrEmail(String username) {
        return userRepository.findByCpf(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new BusinessException("Usuário não encontrado")));
    }

    @Override
    public void addBalance(User user, BigDecimal amount) {
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
    }
}
