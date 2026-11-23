package org.example.service;

/** Abstraction over password encryption so the algorithm can be replaced later. */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String storedHash);
}
