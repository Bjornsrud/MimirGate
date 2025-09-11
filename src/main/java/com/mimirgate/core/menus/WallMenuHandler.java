package com.mimirgate.core.menus;

import com.mimirgate.core.util.TextInputEditor;
import com.mimirgate.core.util.WallRenderer;
import com.mimirgate.model.WallMessage;
import com.mimirgate.service.WallService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.mimirgate.core.util.WallRenderer.renderWall;

public class WallMenuHandler implements MenuHandler {
    private final String menuText;
    private final WallService wallService;
    private final String currentUser;
    private final int terminalWidth;

    public WallMenuHandler(String menuText, WallService wallService, String currentUser, int terminalWidth) {
        this.menuText = menuText;
        this.wallService = wallService;
        this.currentUser = currentUser;
        this.terminalWidth = terminalWidth;
    }

    @Override
    public MenuNav handleCommand(String command, PrintWriter out, BufferedReader in) {
        return switch (command.toUpperCase()) {
            case "?" -> {
                out.println(menuText);
                yield MenuNav.STAY;
            }
            case "V" -> {
                showWall(out, in);
                yield MenuNav.STAY;
            }
            case "N" -> {
                createPost(out, in);
                yield MenuNav.STAY;
            }
            case "M" -> MenuNav.MAIN;
            case "Q" -> MenuNav.DISCONNECT;
            default -> {
                out.println("\n[Wall Command " + command + "] Not implemented yet.");
                yield MenuNav.STAY;
            }
        };
    }

    private void showWall(PrintWriter out, BufferedReader in) {
        List<WallMessage> messages = wallService.getMessagesForWidth(terminalWidth);
        renderWall(out, in, messages, terminalWidth);
    }

    private List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        String remaining = text;
        while (remaining.length() > width) {
            lines.add(remaining.substring(0, width));
            remaining = remaining.substring(width);
        }
        if (!remaining.isEmpty()) {
            lines.add(remaining);
        }
        return lines;
    }

    private void createPost(PrintWriter out, BufferedReader in) {
        try {
            String text = TextInputEditor.promptForText(out, in, "New wall post", wallService.getMaxChars());
            if (text != null && !text.isBlank()) {
                wallService.addMessage(currentUser, text);
                out.println("Message posted!");
            } else {
                out.println("Post canceled or empty.");
            }
        } catch (IOException e) {
            out.println("Error while creating post: " + e.getMessage());
        }
    }

    @Override
    public String getPrompt() {
        return "Message Wall Menu (? for menu) > ";
    }

    @Override
    public String getMenuText() {
        return menuText;
    }
}
