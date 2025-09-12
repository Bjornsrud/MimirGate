package com.mimirgate.core.menus;

import com.mimirgate.model.WallMessage;
import com.mimirgate.service.WallService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SysopMenuHandler implements MenuHandler {
    private final String menuText;
    private final WallService wallService;

    public SysopMenuHandler(String menuText, WallService wallService) {
        this.menuText = menuText;
        this.wallService = wallService;
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

            case "M":
                return MenuNav.MAIN;

            case "Q":
                return MenuNav.DISCONNECT;

            case "U":
                out.println("[Manage Users] Not implemented yet.");
                return MenuNav.STAY;

            case "R":
                out.println("[Change Roles] Not implemented yet.");
                return MenuNav.STAY;

            case "W":
                moderateWall(out, in);
                return MenuNav.STAY;

            case "T":
                out.println("[Moderate Threads] Not implemented yet.");
                return MenuNav.STAY;

            case "P":
                out.println("[Moderate Posts] Not implemented yet.");
                return MenuNav.STAY;

            default:
                out.println("\u0007Unknown command: " + command + "  (type ? for menu)");
                return MenuNav.STAY;
        }
    }

    private void moderateWall(PrintWriter out, BufferedReader in) {
        try {
            List<WallMessage> messages = wallService.findAll();
            if (messages.isEmpty()) {
                out.println("No wall posts found.");
                return;
            }

            out.println("\nWall Posts:");
            for (int i = 0; i < messages.size(); i++) {
                WallMessage msg = messages.get(i);
                out.println(" [" + (i + 1) + "] " + msg.getUsername() + ": " + msg.getContent());
            }

            out.print("Enter post number to delete (blank to cancel): ");
            out.flush();
            String input = in.readLine();

            if (input == null || input.isBlank()) {
                out.println("Canceled.");
                return;
            }

            int index;
            try {
                index = Integer.parseInt(input) - 1;
            } catch (NumberFormatException e) {
                out.println("Invalid number.");
                return;
            }

            if (index < 0 || index >= messages.size()) {
                out.println("Invalid post number.");
                return;
            }

            WallMessage toDelete = messages.get(index);
            wallService.deleteMessage(toDelete.getId());
            out.println("Deleted post by " + toDelete.getUsername() + ": " + toDelete.getContent());

        } catch (IOException e) {
            out.println("Error while moderating wall: " + e.getMessage());
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
