package com.mimirgate.core.menus;

import com.mimirgate.service.UserService;
import com.mimirgate.model.User;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

public class MainMenuHandler implements MenuHandler {
    private final String menuText;
    private final UserService userService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MainMenuHandler(String menuText, UserService userService) {
        this.menuText = menuText;
        this.userService = userService;
    }

    @Override
    public MenuNav handleCommand(String command, PrintWriter out, BufferedReader in) {
        if (command == null || command.isBlank()) {
            return MenuNav.STAY; // ignorer tom input
        }

        switch (command.toUpperCase()) {
            case "?":
                out.println(menuText);
                return MenuNav.STAY;
            case "Q":
                return MenuNav.DISCONNECT;
            case "U":
                return MenuNav.CONFIG;
            case "S":
                return MenuNav.SYSOP;
            case "P":
                return MenuNav.PM;
            case "W":
                return MenuNav.WALL;
            case "K":
                return MenuNav.CONFERENCE;
            case "C":
                out.println("\n[Chat] Not implemented yet. Type ':EXIT:' to leave (placeholder).");
                return MenuNav.STAY;
            case "O":
                out.println("\nOnline Users:\n - Alice\n - Bob\n - Charlie");
                return MenuNav.STAY;
            case "L":
                out.println("\nEnter username for lookup:");
                try {
                    String username = in.readLine();
                    if (username != null && !username.trim().isEmpty()) {
                        userService.findByUsername(username.trim()).ifPresentOrElse(user -> {
                            out.println("\nUser Info:");
                            out.println(" Username   : " + user.getUsername());
                            out.println(" Registered : " + formatter.format(user.getCreatedAt()));
                            out.println(" Role       : " + user.getRole());
                            out.println(" Bio        : " + (user.getBio() != null ? user.getBio() : ""));
                            out.println(" Last login : " + (user.getLastLogin() != null ? formatter.format(user.getLastLogin()) : "Never"));
                        }, () -> out.println("\nUser not found: " + username));
                    } else {
                        out.println("\nNo username provided.");
                    }
                } catch (Exception e) {
                    out.println("\nError reading username.");
                }
                return MenuNav.STAY;
            default:
                out.println("\u0007Unknown command: " + command + "  (type ? for menu)");
                return MenuNav.STAY;
        }
    }

    @Override
    public String getPrompt() {
        return "Main Menu (? for menu) > ";
    }

    @Override
    public String getMenuText() {
        return menuText;
    }
}
