package com.mimirgate.core.menus;

import com.mimirgate.core.SessionHandler;
import com.mimirgate.model.User;
import com.mimirgate.service.ConferenceService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ConferenceAdminMenuHandler implements MenuHandler {
    private final String menuText;
    private final ConferenceService conferenceService;
    private final User currentUser;
    private final SessionHandler sessionHandler;

    public ConferenceAdminMenuHandler(String menuText,
                                      ConferenceService conferenceService,
                                      User currentUser,
                                      SessionHandler sessionHandler) {
        this.menuText = menuText;
        this.conferenceService = conferenceService;
        this.currentUser = currentUser;
        this.sessionHandler = sessionHandler;
    }

    @Override
    public MenuNav handleCommand(String command, PrintWriter out, BufferedReader in) {
        // Blokkér hvis ikke SYSOP eller COSYSOP
        if (!(currentUser.getRole() == User.Role.SYSOP || currentUser.getRole() == User.Role.COSYSOP)) {
            out.println("Access denied: SysOp/CoSysOp only.");
            return MenuNav.CONFERENCE;
        }

        if (command == null || command.isBlank()) {
            return MenuNav.STAY; // ignorer tom input
        }

        switch (command.toUpperCase()) {
            case "?":
                out.println(menuText);
                if (sessionHandler.getCurrentConference() != null) {
                    out.println("Current conference: " + sessionHandler.getCurrentConference().getName());
                }
                return MenuNav.STAY;

            case "B":
                return MenuNav.CONFERENCE;

            case "Q":
                return MenuNav.DISCONNECT;

            case "C": // Create conference
                try {
                    out.print("Enter conference name: ");
                    out.flush();
                    String name = in.readLine();

                    out.print("Enter description: ");
                    out.flush();
                    String desc = in.readLine();

                    if (name == null || name.isBlank()) {
                        out.println("Conference creation canceled.");
                        return MenuNav.STAY;
                    }

                    conferenceService.createConference(name.trim(), desc != null ? desc.trim() : "");
                    out.println("Conference created: " + name);
                } catch (IOException e) {
                    out.println("Error reading input.");
                }
                return MenuNav.STAY;

            case "D": // Delete conference
                try {
                    out.print("Enter conference name to delete: ");
                    out.flush();
                    String name = in.readLine();

                    if (name == null || name.isBlank()) {
                        out.println("Delete canceled.");
                        return MenuNav.STAY;
                    }

                    conferenceService.findByName(name.trim()).ifPresentOrElse(conf -> {
                        // Ikke tillat sletting av current
                        if (sessionHandler.getCurrentConference() != null &&
                                sessionHandler.getCurrentConference().getId().equals(conf.getId())) {
                            out.println("Cannot delete the current active conference: " + conf.getName());
                            return;
                        }

                        try {
                            out.println("WARNING: Deleting a conference will also delete ALL threads and posts!");
                            out.println("This action cannot be undone!");
                            out.print("Are you sure you want to delete '" + conf.getName() + "'? (Y/N): ");
                            out.flush();

                            String confirm = in.readLine();
                            if (confirm != null && confirm.trim().equalsIgnoreCase("Y")) {
                                conferenceService.deleteConference(conf.getId());
                                out.println("Conference deleted: " + conf.getName());
                            } else {
                                out.println("Delete canceled.");
                            }
                        } catch (IOException e) {
                            out.println("Error reading confirmation.");
                        }
                    }, () -> out.println("Conference not found: " + name));

                } catch (IOException e) {
                    out.println("Error reading input.");
                }
                return MenuNav.STAY;

            case "R": // Toggle restricted
                try {
                    out.print("Enter conference name to toggle restricted: ");
                    out.flush();
                    String name = in.readLine();

                    if (name == null || name.isBlank()) {
                        out.println("Action canceled.");
                        return MenuNav.STAY;
                    }

                    conferenceService.findByName(name.trim()).ifPresentOrElse(conf -> {
                        conferenceService.toggleRestricted(conf);
                        out.println("Toggled restricted for: " + conf.getName());
                    }, () -> out.println("Conference not found: " + name));
                } catch (IOException e) {
                    out.println("Error reading input.");
                }
                return MenuNav.STAY;

            case "V": // Toggle VIP-only
                try {
                    out.print("Enter conference name to toggle VIP-only: ");
                    out.flush();
                    String name = in.readLine();

                    if (name == null || name.isBlank()) {
                        out.println("Action canceled.");
                        return MenuNav.STAY;
                    }

                    conferenceService.findByName(name.trim()).ifPresentOrElse(conf -> {
                        conferenceService.toggleVipOnly(conf);
                        out.println("Toggled VIP-only for: " + conf.getName());
                    }, () -> out.println("Conference not found: " + name));
                } catch (IOException e) {
                    out.println("Error reading input.");
                }
                return MenuNav.STAY;

            default:
                out.println("\u0007Unknown command: " + command + "  (type ? for menu)");
                return MenuNav.STAY;
        }
    }

    @Override
    public String getPrompt() {
        if (sessionHandler.getCurrentConference() != null) {
            return "[" + sessionHandler.getCurrentConference().getName() + "] Conference Admin Menu (? for menu) > ";
        }
        return "Conference Admin Menu (? for menu) > ";
    }

    @Override
    public String getMenuText() {
        return menuText;
    }
}
