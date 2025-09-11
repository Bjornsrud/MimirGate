package com.mimirgate.core.menus;

import java.io.PrintWriter;
import java.util.Map;

public class LoginMenuHandler {
    private final Map<String, String> menuTexts40;
    private final Map<String, String> menuTexts80;
    private final int terminalWidth;

    public LoginMenuHandler(Map<String, String> menuTexts40, Map<String, String> menuTexts80, int terminalWidth) {
        this.menuTexts40 = menuTexts40;
        this.menuTexts80 = menuTexts80;
        this.terminalWidth = terminalWidth;
    }

    public void printMenu(PrintWriter out) {
        String menuText = terminalWidth == 40 ? menuTexts40.get("LOGIN") : menuTexts80.get("LOGIN");
        if (menuText != null) {
            out.println(menuText);
        } else {
            out.println("Login menu not found for width: " + terminalWidth);
        }
        out.flush();
    }

    public boolean handleCommand(String command) {
        return !("40".equalsIgnoreCase(command) || "80".equalsIgnoreCase(command));
    }
}
