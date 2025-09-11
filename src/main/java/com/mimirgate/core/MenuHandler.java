package com.mimirgate.core;

import java.io.BufferedReader;
import java.io.PrintWriter;

public interface MenuHandler {
    MenuNav handleCommand(String command, PrintWriter out, BufferedReader in);

    String getPrompt();
    String getMenuText();
}
