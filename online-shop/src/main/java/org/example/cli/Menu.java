package org.example.cli;

import java.util.List;
import org.example.model.exception.DomainException;

/** Base class of all screens: renders a numbered menu and runs the chosen action safely. */
public abstract class Menu {

    protected final ConsoleIO io;

    protected Menu(ConsoleIO io) {
        this.io = io;
    }

    /** Runs this screen until the user leaves it. */
    public abstract void open();

    /** Shows the menu once; returns {@code false} when the user chose to leave it. */
    protected boolean showOnce(String title, List<MenuItem> items, String exitLabel) {
        io.println();
        io.println("=== " + title + " ===");
        for (int i = 0; i < items.size(); i++) {
            io.println((i + 1) + ". " + items.get(i).label());
        }
        io.println("0. " + exitLabel);

        int choice = io.readInt("Your choice");
        if (choice == 0) {
            return false;
        }
        if (choice < 1 || choice > items.size()) {
            io.printError("Invalid option.");
            return true;
        }
        runSafely(items.get(choice - 1).action());
        return true;
    }

    protected void loop(String title, List<MenuItem> items) {
        boolean keepGoing = true;
        while (keepGoing) {
            keepGoing = showOnce(title, items, "Back");
        }
    }

    protected void runSafely(Runnable action) {
        try {
            action.run();
        } catch (DomainException e) {
            io.printError(e.getMessage());
        }
    }
}
