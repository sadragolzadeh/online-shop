package org.example.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;

/** Thin wrapper around the terminal: all reading and writing goes through here. */
public class ConsoleIO {

    private static final Set<String> YES_ANSWERS = Set.of("y", "yes");

    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleIO(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in, StandardCharsets.UTF_8);
        this.out = out;
    }

    public void println() {
        out.println();
    }

    public void println(String text) {
        out.println(text);
    }

    public void printError(String message) {
        out.println("Error: " + message);
    }

    public void printSuccess(String message) {
        out.println("Success: " + message);
    }

    public String readLine(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
        return readRequired(prompt, Integer::parseInt);
    }

    public BigDecimal readDecimal(String prompt) {
        return readRequired(prompt, BigDecimal::new);
    }

    public <T> T readRequired(String prompt, Function<String, T> parser) {
        while (true) {
            try {
                return parser.apply(readLine(prompt));
            } catch (NumberFormatException e) {
                printError("The entered value is not valid.");
            }
        }
    }

    /** Returns {@code null} when the user just presses Enter. */
    public <T> T readOptional(String prompt, Function<String, T> parser) {
        while (true) {
            String line = readLine(prompt + " (optional, press Enter to skip)");
            if (line.isEmpty()) {
                return null;
            }
            try {
                return parser.apply(line);
            } catch (NumberFormatException e) {
                printError("The entered value is not valid.");
            }
        }
    }

    public boolean confirm(String prompt) {
        return YES_ANSWERS.contains(readLine(prompt + " (y/n)").toLowerCase());
    }
}
