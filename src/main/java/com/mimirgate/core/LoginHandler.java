package com.mimirgate.core;

import com.mimirgate.core.util.TextInputEditor;
import com.mimirgate.model.LoginResult;
import com.mimirgate.model.User;
import com.mimirgate.service.UserService;
import com.mimirgate.core.menus.LoginMenuHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;

public class LoginHandler {
    private final UserService userService;
    private final Map<String, String> menuTexts40;
    private final Map<String, String> menuTexts80;
    private int terminalWidth = 40; // 40 er default

    public LoginHandler(UserService userService, Map<String, String> menuTexts40, Map<String, String> menuTexts80) {
        this.userService = userService;
        this.menuTexts40 = menuTexts40;
        this.menuTexts80 = menuTexts80;
    }

    public Optional<LoginResult> handleLogin(PrintWriter out, BufferedReader in) throws IOException {
        while (true) {
            LoginMenuHandler menu = new LoginMenuHandler(menuTexts40, menuTexts80, terminalWidth);
            menu.printMenu(out);

            out.print("Choice: ");
            out.flush();
            String choice = in.readLine();
            if (choice == null) {
                return Optional.of(new LoginResult(null, terminalWidth, LoginResult.LoginStatus.DISCONNECT));
            }

            switch (choice.trim().toUpperCase()) {
                case "40":
                    terminalWidth = 40;
                    break;
                case "80":
                    terminalWidth = 80;
                    break;
                case "L":
                    Optional<User> userOpt = login(out, in);
                    if (userOpt.isPresent()) {
                        return Optional.of(new LoginResult(userOpt.get(), terminalWidth, LoginResult.LoginStatus.SUCCESS));
                    }
                    return Optional.of(new LoginResult(null, terminalWidth, LoginResult.LoginStatus.RETRY));
                case "R":
                    Optional<User> newUser = register(out, in);
                    if (newUser.isPresent()) {
                        return Optional.of(new LoginResult(newUser.get(), terminalWidth, LoginResult.LoginStatus.SUCCESS));
                    }
                    return Optional.of(new LoginResult(null, terminalWidth, LoginResult.LoginStatus.RETRY));
                case "D":
                    out.println("Goodbye!");
                    return Optional.of(new LoginResult(null, terminalWidth, LoginResult.LoginStatus.DISCONNECT));
                default:
                    out.println("Invalid choice. Try again.");
            }
        }
    }

    private Optional<User> login(PrintWriter out, BufferedReader in) throws IOException {
        out.print("Username: ");
        out.flush();
        String username = in.readLine();

        if (username == null) return Optional.empty();

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            out.print("User not found. Do you want to register this username? (Y/N): ");
            out.flush();
            String choice = in.readLine();
            if (choice != null && choice.trim().equalsIgnoreCase("Y")) {
                return registerWithPredefinedUsername(out, in, username);
            } else {
                out.println("Returning to login menu...");
                return Optional.empty(); // Gå tilbake til login-menyen, ikke avslutt sesjonen
            }
        }

        int attempts = 3;
        while (attempts > 0) {
            out.print("Password: ");
            out.flush();
            String password = in.readLine();
            if (password == null) return Optional.empty();

            if (password.equals(userOpt.get().getPasswordHash())) {
                out.println("Login successful. Welcome " + username + "!");
                terminalWidth = userOpt.get().getTerminalWidth(); // Sett brukerens preferanse
                return userOpt;
            } else {
                attempts--;
                if (attempts > 0) {
                    out.println("Invalid password. Attempts left: " + attempts);
                }
            }
        }

        out.println("Too many failed attempts. Returning to login menu.");
        return Optional.empty(); // Ikke avslutt sesjonen, gå tilbake til login-menyen
    }

    private Optional<User> registerWithPredefinedUsername(PrintWriter out, BufferedReader in, String username) throws IOException {
        out.println("Registering user: " + username);

        String password = null;
        while (true) {
            out.print("Choose a password: ");
            out.flush();
            String pass1 = in.readLine();
            out.print("Repeat password: ");
            out.flush();
            String pass2 = in.readLine();

            if (pass1 == null || pass2 == null || pass1.isEmpty()) {
                out.println("Password cannot be empty.");
                return Optional.empty();
            }
            if (!pass1.equals(pass2)) {
                out.println("Passwords do not match. Try again.");
            } else {
                password = pass1;
                break;
            }
        }

        out.print("Enter your email: ");
        out.flush();
        String email = in.readLine();
        if (email == null) email = "";

        String bio = TextInputEditor.promptForText(out, in, "Enter your bio", 256);
        if (bio == null) {
            out.println("Bio entry canceled. Registration aborted.");
            return Optional.empty();
        }

        out.print("Choose default terminal width [40/80]: ");
        out.flush();
        String widthStr = in.readLine();
        int defaultWidth = "80".equals(widthStr) ? 80 : 40;

        User user = userService.createUser(username, password, email, bio, defaultWidth);
        out.println("Registration successful. Welcome " + username + "!");
        terminalWidth = defaultWidth;
        return Optional.of(user);
    }

    private Optional<User> register(PrintWriter out, BufferedReader in) throws IOException {
        out.print("Choose a username: ");
        out.flush();
        String username = in.readLine();
        if (username == null || username.isEmpty()) {
            out.println("Username cannot be empty.");
            return Optional.empty();
        }

        if (userService.usernameExists(username)) {
            out.println("Username already taken.");
            return Optional.empty();
        }

        String password = null;
        while (true) {
            out.print("Choose a password: ");
            out.flush();
            String pass1 = in.readLine();
            out.print("Repeat password: ");
            out.flush();
            String pass2 = in.readLine();

            if (pass1 == null || pass2 == null || pass1.isEmpty()) {
                out.println("Password cannot be empty.");
                return Optional.empty();
            }
            if (!pass1.equals(pass2)) {
                out.println("Passwords do not match. Try again.");
            } else {
                password = pass1;
                break;
            }
        }

        out.print("Enter your email: ");
        out.flush();
        String email = in.readLine();
        if (email == null) email = "";

        String bio = TextInputEditor.promptForText(out, in, "Enter your bio", 256);
        if (bio == null) {
            out.println("Bio entry canceled. Registration aborted.");
            return Optional.empty();
        }

        out.print("Choose default terminal width [40/80]: ");
        out.flush();
        String widthStr = in.readLine();
        int defaultWidth = "80".equals(widthStr) ? 80 : 40;


        User user = userService.createUser(username, password, email, bio, defaultWidth);
        out.println("Registration successful. Welcome " + username + "!");
        terminalWidth = defaultWidth; // Bruker dette for resten av sesjonen
        return Optional.of(user);
    }
}
