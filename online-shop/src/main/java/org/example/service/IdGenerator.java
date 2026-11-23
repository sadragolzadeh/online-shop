package org.example.service;

import java.util.concurrent.atomic.AtomicLong;

/** Produces short readable identifiers such as {@code P-1}, {@code P-2}. */
public class IdGenerator {

    private final String prefix;
    private final AtomicLong counter = new AtomicLong();

    public IdGenerator(String prefix) {
        this.prefix = prefix;
    }

    public String next() {
        return prefix + "-" + counter.incrementAndGet();
    }
}
