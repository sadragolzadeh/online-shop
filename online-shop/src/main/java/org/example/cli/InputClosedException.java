package org.example.cli;

/** Thrown when the input stream ends (for example Ctrl+D) while the program waits for input. */
public class InputClosedException extends RuntimeException {

    public InputClosedException() {
        super("Input stream closed");
    }
}
