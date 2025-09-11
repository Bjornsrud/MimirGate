package com.mimirgate.core;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class MainMenuHandler implements MenuHandler {
    private final String menuText;

    public MainMenuHandler(String menuText) {
        this.menuText = menuText;
    }

    @Override
    public MenuNav handleCommand(String command, PrintWriter out, BufferedReader in) {
        switch (command.toUpperCase()) {
            case "?": out.println(menuText); return MenuNav.STAY;
            case "Q": return MenuNav.DISCONNECT;
            case "U": return MenuNav.CONFIG;
            case "S": return MenuNav.SYSOP;
            case "P": return MenuNav.PM;
            case "W": return MenuNav.WALL;
            case "F": return MenuNav.FILE;
            case "C":
                out.println("\n[Chat] Not implemented yet. Type ':EXIT:' to leave (placeholder).");
                return MenuNav.STAY;
            case "O":
                out.println("\nOnline Users:\n - Alice\n - Bob\n - Charlie");
                return MenuNav.STAY;
            case "L":
                out.println("\nEnter username for WHOIS lookup:");
                try {
                    String user = in.readLine();
                    if (user != null && !user.trim().isEmpty()) {
                        out.println("\nUser Info for " + user + ":\n - Real name: (placeholder)\n - Bio: (placeholder)\n - Last login: (placeholder)");
                    } else {
                        out.println("\nNo username provided.");
                    }
                } catch (Exception e) {
                    out.println("\nError reading username.");
                }
                return MenuNav.STAY;
            default:
                out.println("\n[Main Command " + command + "] Not implemented yet.");
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
