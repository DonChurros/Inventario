package com.example.inventario.infrastructure.adapter.in.web;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthenticationSessionRegistry {
    private static final Duration SESSION_DURATION = Duration.ofHours(8);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public Session create(int employeeId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Session session = new Session(employeeId, Instant.now().plus(SESSION_DURATION));
        sessions.put(token, session);
        return new Session(employeeId, session.expiresAt(), token);
    }

    public Session find(String token) {
        if (token == null) {
            return null;
        }
        Session session = sessions.get(token);
        if (session == null) {
            return null;
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token, session);
            return null;
        }
        return session;
    }

    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public record Session(int employeeId, Instant expiresAt, String token) {
        public Session(int employeeId, Instant expiresAt) {
            this(employeeId, expiresAt, null);
        }
    }
}
