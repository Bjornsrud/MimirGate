package com.mimirgate.core;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class SysopMenuHandler implements MenuHandler {
    private final String menuText;

    public SysopMenuHandler(String menuText) {
        this.menuText = menuText;
    }

    @Override
    public MenuNav handleCommand(String command, PrintWriter out, BufferedReader in) {
        switch (command.toUpperCase()) {
            case "?": out.println(menuText); return MenuNav.STAY;
            case "M": return MenuNav.MAIN;
            case "Q": return MenuNav.DISCONNECT;
            default: out.println("\n[Sysop Command " + command + "] Not implemented yet."); return MenuNav.STAY;
        }
    }

    @Override
    public String getPrompt() {
        return "Sysop Menu (? for menu) > ";
    }

    @Override
    public String getMenuText() {
        return menuText;
    }
}
