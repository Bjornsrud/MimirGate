package com.mimirgate.core.menus;

import com.mimirgate.core.util.TextInputEditor;
import com.mimirgate.core.util.WallRenderer;
import com.mimirgate.model.WallMessage;
import com.mimirgate.service.WallService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
            case "L" -> {
                showLastPosts(out, in, 20);
                yield MenuNav.STAY;
            }
            case "U" -> {
                showByUser(out, in);
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

    private void showLastPosts(PrintWriter out, BufferedReader in, int count) {
        List<WallMessage> all = wallService.getMessagesForWidth(terminalWidth);
        int fromIndex = Math.max(0, all.size() - count);
        List<WallMessage> last = all.subList(fromIndex, all.size());
        renderWall(out, in, last, terminalWidth);
    }

    private void showByUser(PrintWriter out, BufferedReader in) {
        try {
            out.print("Enter username: ");
            out.flush();
            String user = in.readLine();
            if (user == null || user.isBlank()) {
                out.println("Canceled.");
                return;
            }
            List<WallMessage> all = wallService.getMessagesForWidth(terminalWidth);
            List<WallMessage> filtered = all.stream()
                    .filter(m -> m.getUsername().equalsIgnoreCase(user.trim()))
                    .toList();
            if (filtered.isEmpty()) {
                out.println("No posts found for user: " + user);
                return;
            }
            renderWall(out, in, filtered, terminalWidth);
        } catch (IOException e) {
            out.println("Error: " + e.getMessage());
        }
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
