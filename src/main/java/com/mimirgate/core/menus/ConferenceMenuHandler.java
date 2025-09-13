package com.mimirgate.core.menus;

import com.mimirgate.core.SessionHandler;
import com.mimirgate.core.util.Pager;
import com.mimirgate.core.util.TextInputEditor;
import com.mimirgate.model.User;
import com.mimirgate.model.Conference;
import com.mimirgate.model.ThreadEntity;
import com.mimirgate.model.PostEntity;
import com.mimirgate.service.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConferenceMenuHandler implements MenuHandler {
    private final String menuText;
    private final ConferenceService conferenceService;
    private final ThreadService threadService;
    private final PostService postService;
    private final User currentUser;
    private final SessionHandler sessionHandler;
    private final String howtoText;

    public ConferenceMenuHandler(String menuText,
                                 ConferenceService conferenceService,
                                 ThreadService threadService,
                                 PostService postService,
                                 User currentUser,
                                 SessionHandler sessionHandler) {
        this.menuText = menuText;
        this.conferenceService = conferenceService;
        this.threadService = threadService;
        this.postService = postService;
        this.currentUser = currentUser;
        this.sessionHandler = sessionHandler;

        this.howtoText = sessionHandler != null
                ? sessionHandler.getActiveMenus().getOrDefault("CONF_HOWTO", "No howto text found.")
                : "No howto text found.";
    }

    @Override
    public MenuNav handleCommand(String command, PrintWriter out, BufferedReader in) {
        if (command == null || command.isBlank()) {
            return MenuNav.STAY;
        }

        String[] parts = command.trim().split("\\s+");
        String base = parts[0].toUpperCase();

        switch (base) {
            case "?":
                out.println(menuText);
                return MenuNav.STAY;
            case "M":
                return MenuNav.MAIN;
            case "Q":
                return MenuNav.DISCONNECT;
            case "C":
                listConferences(out);
                return MenuNav.STAY;
            case "L":
                if (parts.length > 1) {
                    listPostsRange(out, in, parts[1]);
                } else {
                    listPosts(out, in);
                }
                return MenuNav.STAY;
            case "N":
                newPost(out, in);
                return MenuNav.STAY;
            case "R":
                if (parts.length > 1) {
                    replyToPost(out, in, parts[1]);
                } else {
                    replyToPost(out, in, null);
                }
                return MenuNav.STAY;
            case "J":
                joinConference(out, in);
                return MenuNav.STAY;
            case "H":
                printHowto(out);
                return MenuNav.STAY;
            case "A":
                if (currentUser.getRole() == User.Role.SYSOP || currentUser.getRole() == User.Role.COSYSOP) {
                    return MenuNav.CONF_ADMIN;
                } else {
                    out.println("Access denied: SysOp/CosysOp only.");
                    return MenuNav.STAY;
                }
            default:
                out.println("\u0007Unknown command: " + command + "  (type ? for menu)");
                return MenuNav.STAY;
        }
    }

    private void listConferences(PrintWriter out) {
        out.println("\nAvailable conferences:");
        List<Conference> all = conferenceService.listAll();
        if (all.isEmpty()) {
            out.println(" (none found)");
            return;
        }

        for (Conference conf : all) {
            if (!conferenceService.hasAccess(currentUser, conf)) {
                continue;
            }

            boolean isCurrent = sessionHandler.getCurrentConference() != null
                    && sessionHandler.getCurrentConference().getId().equals(conf.getId());

            String flags = "";
            if (conf.isRestricted()) flags += " [restricted]";
            if (conf.isVipOnly()) flags += " [vip]";
            if (isCurrent) flags += " (current)";

            out.println(" - " + conf.getName() + flags);
        }
    }

    private void listPosts(PrintWriter out, BufferedReader in) {
        Conference conf = sessionHandler.getCurrentConference();
        if (conf == null) {
            out.println("No current conference selected.");
            return;
        }

        List<ThreadEntity> threads = threadService.listThreads(conf);
        if (threads.isEmpty()) {
            out.println("No posts in this conference.");
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add("\nPosts in " + conf.getName() + ":");
        for (ThreadEntity thread : threads) {
            lines.add("Topic: " + thread.getTitle() + " (by " + thread.getCreator().getUsername() + ")");
            List<PostEntity> posts = postService.listPosts(thread);
            for (PostEntity p : posts) {
                lines.add("   [" + p.getAuthor().getUsername() + "] " + p.getContent());
            }
            lines.add("");
        }

        try {
            Pager.display(lines, out, in);
        } catch (IOException e) {
            out.println("Error displaying posts.");
        }
    }

    private void listPostsRange(PrintWriter out, BufferedReader in, String range) {
        Conference conf = sessionHandler.getCurrentConference();
        if (conf == null) {
            out.println("No current conference selected.");
            return;
        }

        List<ThreadEntity> threads = threadService.listThreads(conf);
        if (threads.isEmpty()) {
            out.println("No posts in this conference.");
            return;
        }

        String[] parts = range.split("-");
        int start, end;
        try {
            start = Integer.parseInt(parts[0]) - 1;
            end = (parts.length > 1) ? Integer.parseInt(parts[1]) : start + 1;
        } catch (NumberFormatException e) {
            out.println("Invalid range format. Use e.g. L 10-20");
            return;
        }

        List<String> lines = new ArrayList<>();
        for (int i = start; i < end && i < threads.size(); i++) {
            ThreadEntity thread = threads.get(i);
            lines.add("Topic: " + thread.getTitle() + " (by " + thread.getCreator().getUsername() + ")");
            List<PostEntity> posts = postService.listPosts(thread);
            for (PostEntity p : posts) {
                lines.add("   [" + p.getAuthor().getUsername() + "] " + p.getContent());
            }
            lines.add("");
        }

        try {
            Pager.display(lines, out, in);
        } catch (IOException e) {
            out.println("Error displaying posts.");
        }
    }

    private void newPost(PrintWriter out, BufferedReader in) {
        Conference conf = sessionHandler.getCurrentConference();
        if (conf == null) {
            out.println("No current conference selected.");
            return;
        }

        if (!conferenceService.hasAccess(currentUser, conf)) {
            out.println("Access denied: you do not have permission to post in this conference.");
            return;
        }

        try {
            out.print("Enter topic: ");
            out.flush();
            String subject = in.readLine();
            if (subject == null || subject.isBlank()) {
                out.println("Post canceled.");
                return;
            }

            ThreadEntity thread = threadService.createThread(conf, currentUser, subject.trim());
            String content = TextInputEditor.promptForText(out, in, "Enter post content", 4000);
            if (content != null && !content.isBlank()) {
                postService.createPost(thread, currentUser, content.trim());
                out.println("New post created in " + conf.getName() + ": " + subject);
            } else {
                out.println("Empty post. Thread not created.");
            }
        } catch (IOException e) {
            out.println("Error while creating post: " + e.getMessage());
        }
    }

    private void replyToPost(PrintWriter out, BufferedReader in, String range) {
        Conference conf = sessionHandler.getCurrentConference();
        if (conf == null) {
            out.println("No current conference selected.");
            return;
        }

        if (!conferenceService.hasAccess(currentUser, conf)) {
            out.println("Access denied: you do not have permission to post in this conference.");
            return;
        }

        List<ThreadEntity> threads = threadService.listThreads(conf);
        if (threads.isEmpty()) {
            out.println("No threads to reply to.");
            return;
        }

        List<String> lines = new ArrayList<>();
        if (range != null) {
            String[] parts = range.split("-");
            int start, end;
            try {
                start = Integer.parseInt(parts[0]) - 1;
                end = (parts.length > 1) ? Integer.parseInt(parts[1]) : start + 1;
            } catch (NumberFormatException e) {
                out.println("Invalid range format. Use e.g. R 10-20");
                return;
            }
            for (int i = start; i < end && i < threads.size(); i++) {
                lines.add(" [" + (i + 1) + "] " + threads.get(i).getTitle());
            }
        } else {
            for (int i = 0; i < threads.size(); i++) {
                lines.add(" [" + (i + 1) + "] " + threads.get(i).getTitle());
            }
        }

        try {
            Pager.display(lines, out, in);
        } catch (IOException e) {
            out.println("Error displaying threads.");
            return;
        }

        try {
            out.print("Choose thread number: ");
            out.flush();
            String input = in.readLine();
            int idx;
            try {
                idx = Integer.parseInt(input) - 1;
            } catch (NumberFormatException e) {
                out.println("Invalid input.");
                return;
            }
            if (idx < 0 || idx >= threads.size()) {
                out.println("Invalid thread number.");
                return;
            }

            ThreadEntity thread = threads.get(idx);
            String content = TextInputEditor.promptForText(out, in, "Reply to " + thread.getTitle(), 4000);
            if (content != null && !content.isBlank()) {
                postService.createPost(thread, currentUser, content.trim());
                out.println("Reply posted to thread: " + thread.getTitle());
            } else {
                out.println("Reply canceled.");
            }
        } catch (IOException e) {
            out.println("Error while replying: " + e.getMessage());
        }
    }

    private void joinConference(PrintWriter out, BufferedReader in) {
        try {
            out.print("Enter conference name to switch to: ");
            out.flush();
            String name = in.readLine();
            if (name == null || name.isBlank()) {
                out.println("Switch canceled.");
                return;
            }
            Optional<Conference> confOpt = conferenceService.findByName(name.trim());
            if (confOpt.isEmpty()) {
                out.println("Conference not found: " + name);
                return;
            }
            Conference conf = confOpt.get();
            if (!conferenceService.hasAccess(currentUser, conf)) {
                out.println("Access denied: you do not have permission to enter this conference.");
                return;
            }
            sessionHandler.setCurrentConference(conf);
            out.println("Switched current conference to: " + conf.getName());
        } catch (IOException e) {
            out.println("Error reading input.");
        }
    }

    private void printHowto(PrintWriter out) {
        out.println("\n" + howtoText);
    }

    @Override
    public String getPrompt() {
        return "Conference Menu (? for menu) > ";
    }

    @Override
    public String getMenuText() {
        return menuText;
    }
}
