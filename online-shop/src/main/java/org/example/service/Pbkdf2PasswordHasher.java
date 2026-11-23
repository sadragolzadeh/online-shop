package org.example.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Salted PBKDF2 hashing; stored format is {@code base64(salt):base64(hash)}. */
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        byte[] hash = derive(rawPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        return MessageDigest.isEqual(expected, derive(rawPassword, salt));
    }

    private byte[] derive(String rawPassword, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available", e);
        }
    }
}
