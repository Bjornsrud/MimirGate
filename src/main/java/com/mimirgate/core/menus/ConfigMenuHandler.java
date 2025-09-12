package com.mimirgate.core.menus;

import com.mimirgate.core.util.TextInputEditor;
import com.mimirgate.model.User;
import com.mimirgate.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ConfigMenuHandler implements MenuHandler {
    private final String menuText;
    private final UserService userService;
    private final User currentUser;

    public ConfigMenuHandler(String menuText, UserService userService, User currentUser) {
        this.menuText = menuText;
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @Override
    public MenuNav handleCommand(String command, PrintWriter out, BufferedReader in) {
        try {

            if (command == null || command.isBlank()) {
                return MenuNav.STAY; // ignorer tom input
            }

            switch (command.toUpperCase()) {
                case "?":
                    out.println(menuText);
                    return MenuNav.STAY;
                case "M":
                    return MenuNav.MAIN;
                case "Q":
                    return MenuNav.DISCONNECT;
                case "U":
                    handleUpdateBio(out, in);
                    return MenuNav.STAY;
                case "E":
                    handleUpdateEmail(out, in);
                    return MenuNav.STAY;
                case "P":
                    handleChangePassword(out, in);
                    return MenuNav.STAY;
                case "D":
                    handleTerminalSettings(out, in);
                    return MenuNav.STAY;
                case "S":
                    handleShowSettings(out);
                    return MenuNav.STAY;
                default:
                    out.println("\u0007Unknown command: " + command + "  (type ? for menu)");
                    return MenuNav.STAY;
            }
        } catch (IOException e) {
            out.println("Error reading input: " + e.getMessage());
            return MenuNav.STAY;
        }
    }

    private void handleUpdateBio(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Current bio: " + (currentUser.getBio() != null ? currentUser.getBio() : "(none)"));
        String newBio = TextInputEditor.promptForText(out, in, "Enter new bio", 256);
        if (newBio != null) {
            userService.updateBio(currentUser, newBio);
            out.println("Bio updated.");
        } else {
            out.println("Bio update canceled.");
        }
    }

    private void handleUpdateEmail(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Current email: " + (currentUser.getEmail() != null ? currentUser.getEmail() : "(none)"));
        out.print("Enter new email (leave blank to cancel): ");
        out.flush();
        String email = in.readLine();
        if (email != null && !email.trim().isEmpty()) {
            userService.updateEmail(currentUser, email.trim());
            out.println("Email updated.");
        } else {
            out.println("Email update canceled.");
        }
    }

    private void handleChangePassword(PrintWriter out, BufferedReader in) throws IOException {
        out.print("Enter new password (leave blank to cancel): ");
        out.flush();
        String pass1 = in.readLine();
        if (pass1 == null || pass1.isBlank()) {
            out.println("Password update canceled.");
            return;
        }

        out.print("Re-enter new password: ");
        out.flush();
        String pass2 = in.readLine();
        if (pass2 == null || pass2.isBlank()) {
            out.println("Password update canceled.");
            return;
        }

        if (!pass1.equals(pass2)) {
            out.println("Passwords do not match. Update canceled.");
            return;
        }

        userService.updatePassword(currentUser, pass1);
        out.println("Password updated successfully.");
    }

    private void handleTerminalSettings(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Current terminal width: " + currentUser.getTerminalWidth());
        out.print("Enter terminal width (40 or 80, leave blank to cancel): ");
        out.flush();
        String input = in.readLine();
        if (input == null || input.isBlank()) {
            out.println("Canceled.");
            return;
        }
        try {
            int width = Integer.parseInt(input.trim());
            if (width == 40 || width == 80) {
                userService.updateTerminalWidth(currentUser, width);
                out.println("Terminal width updated to " + width + ".");
            } else {
                out.println("Invalid width. Must be 40 or 80.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Must be 40 or 80.");
        }
    }

    private void handleShowSettings(PrintWriter out) {
        out.println("\nCurrent user settings:");
        out.println(" Username       : " + currentUser.getUsername());
        out.println(" Email          : " + (currentUser.getEmail() != null ? currentUser.getEmail() : "(none)"));
        out.println(" Bio            : " + (currentUser.getBio() != null ? currentUser.getBio() : "(none)"));
        out.println(" Terminal width : " + currentUser.getTerminalWidth());
        out.println(" Role           : " + currentUser.getRole());
    }

    @Override
    public String getPrompt() {
        return "Config Menu (? for menu) > ";
    }

    @Override
    public String getMenuText() {
        return menuText;
    }
}
