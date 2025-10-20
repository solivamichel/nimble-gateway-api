package com.nimble.gateway.security;

import com.nimble.gateway.entity.User;
import com.nimble.gateway.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByCpf(username)
                .orElseGet(() -> userRepository.findByEmail(username).orElse(null));
        if (u == null) throw new UsernameNotFoundException("User not found");
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getCpf())
                .password(u.getPasswordHash())
                .authorities(Collections.emptyList())
                .build();
    }
}
