package com.nimble.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {

    private final Key key;
    private final long expirationMs;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expirationMs}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(String subject){
        Date now = new Date(), exp = new Date(now.getTime()+expirationMs);
        return Jwts.builder().setSubject(subject).setIssuedAt(now).setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public String getSubject(String token){
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}
