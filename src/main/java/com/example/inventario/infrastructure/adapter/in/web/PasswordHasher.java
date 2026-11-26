package com.example.inventario.infrastructure.adapter.in.web;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class PasswordHasher {
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            return "pbkdf2-sha256$" + ITERATIONS + "$"
                    + Base64.getEncoder().withoutPadding().encodeToString(salt) + "$"
                    + Base64.getEncoder().withoutPadding().encodeToString(hash);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo proteger la contraseña del empleado.", exception);
        } finally {
            spec.clearPassword();
        }
    }

    static boolean matches(String password, String encoded) {
        if (password == null || encoded == null) {
            return false;
        }
        String[] parts = encoded.split("\\$", -1);
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100_000 || iterations > 1_000_000) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (salt.length != SALT_BYTES || expected.length != KEY_BITS / 8) {
                return false;
            }
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            try {
                byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec)
                        .getEncoded();
                return MessageDigest.isEqual(expected, actual);
            } finally {
                spec.clearPassword();
            }
        } catch (IllegalArgumentException exception) {
            return false;
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo verificar la contraseña del empleado.", exception);
        }
    }

    static boolean matchesLegacyPlaintext(String password, String storedValue) {
        if (password == null || storedValue == null || storedValue.isBlank() || storedValue.contains("$")) {
            return false;
        }
        return MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                storedValue.getBytes(StandardCharsets.UTF_8)
        );
    }
}
