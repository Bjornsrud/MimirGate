package com.mimirgate.core.menus;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class PmMenuHandler implements MenuHandler {
    private final String menuText;

    public PmMenuHandler(String menuText) {
        this.menuText = menuText;
    }

    @Override
    public MenuNav handleCommand(String command, PrintWriter out, BufferedReader in) {

        if (command == null || command.isBlank()) {
            return MenuNav.STAY; // ignorer tom input
        }

        switch (command.toUpperCase()) {
            case "?": out.println(menuText); return MenuNav.STAY;
            case "M": return MenuNav.MAIN;
            case "Q": return MenuNav.DISCONNECT;
            default:
                out.println("\u0007Unknown command: " + command + "  (type ? for menu)");
                return MenuNav.STAY;
        }
    }

    @Override
    public String getPrompt() {
        return "Private Messages Menu (? for menu) > ";
    }

    @Override
    public String getMenuText() {
        return menuText;
    }
}
