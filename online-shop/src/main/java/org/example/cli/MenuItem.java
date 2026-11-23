package org.example.cli;

/** One numbered entry of a menu. */
public record MenuItem(String label, Runnable action) {
}
