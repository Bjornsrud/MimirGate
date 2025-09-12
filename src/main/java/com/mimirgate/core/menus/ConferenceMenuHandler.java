package com.mimirgate.core.menus;

import com.mimirgate.core.util.TextInputEditor;
import com.mimirgate.model.User;
import com.mimirgate.model.Conference;
import com.mimirgate.model.ThreadEntity;
import com.mimirgate.model.PostEntity;
import com.mimirgate.service.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

public class ConferenceMenuHandler implements MenuHandler {
    private final String menuText;
    private final ConferenceService conferenceService;
    private final ConferenceMembershipService membershipService;
    private final ThreadService threadService;
    private final PostService postService;
    private final User currentUser;

    public ConferenceMenuHandler(String menuText,
                                 ConferenceService conferenceService,
                                 ConferenceMembershipService membershipService,
                                 ThreadService threadService,
                                 PostService postService,
                                 User currentUser) {
        this.menuText = menuText;
        this.conferenceService = conferenceService;
        this.membershipService = membershipService;
        this.threadService = threadService;
        this.postService = postService;
        this.currentUser = currentUser;
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
            case "C":
                listConferences(out);
                return MenuNav.STAY;
            case "L":
                listPosts(out, in);
                return MenuNav.STAY;
            case "J":
                joinConference(out, in);
                return MenuNav.STAY;
            case "K":
                leaveConference(out, in);
                return MenuNav.STAY;
            case "N":
                newPost(out, in);
                return MenuNav.STAY;
            case "R":
                replyToPost(out, in);
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
                continue; // ikke synlig for denne brukeren
            }

            boolean isMember = membershipService.isMember(currentUser, conf);

            String flags = "";
            if (conf.isRestricted()) flags += " [restricted]";
            if (conf.isVipOnly()) flags += " [vip]";
            if (isMember) flags += " (joined)";

            out.println(" - " + conf.getName() + flags);
        }
    }

    private void listPosts(PrintWriter out, BufferedReader in) {
        try {
            out.print("Enter conference name to view posts: ");
            out.flush();
            String name = in.readLine();
            if (name == null || name.isBlank()) {
                out.println("Canceled.");
                return;
            }
            Optional<Conference> confOpt = conferenceService.findByName(name.trim());
            if (confOpt.isEmpty()) {
                out.println("Conference not found: " + name);
                return;
            }
            Conference conf = confOpt.get();
            if (!membershipService.isMember(currentUser, conf)) {
                out.println("You are not a member of this conference.");
                return;
            }

            List<ThreadEntity> threads = threadService.listThreads(conf);
            if (threads.isEmpty()) {
                out.println("No posts in this conference.");
                return;
            }

            out.println("\nPosts in " + conf.getName() + ":");
            for (ThreadEntity thread : threads) {
                out.println("Subject: " + thread.getTitle() + " (by " + thread.getCreator().getUsername() + ")");
                List<PostEntity> posts = postService.listPosts(thread);
                for (PostEntity p : posts) {
                    out.println("   [" + p.getAuthor().getUsername() + "] " + p.getContent());
                }
                out.println();
            }
        } catch (IOException e) {
            out.println("Error reading input.");
        }
    }

    private void newPost(PrintWriter out, BufferedReader in) {
        try {
            out.print("Enter conference name to post in: ");
            out.flush();
            String name = in.readLine();
            if (name == null || name.isBlank()) {
                out.println("Canceled.");
                return;
            }
            Optional<Conference> confOpt = conferenceService.findByName(name.trim());
            if (confOpt.isEmpty()) {
                out.println("Conference not found: " + name);
                return;
            }
            Conference conf = confOpt.get();
            if (!membershipService.isMember(currentUser, conf)) {
                out.println("You are not a member of this conference.");
                return;
            }

            out.print("Enter subject: ");
            out.flush();
            String subject = in.readLine();
            if (subject == null || subject.isBlank()) {
                out.println("Post canceled.");
                return;
            }

            // Opprett thread
            ThreadEntity thread = threadService.createThread(conf, currentUser, subject.trim());

            // Første post via editor
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

    private void replyToPost(PrintWriter out, BufferedReader in) {
        try {
            out.print("Enter conference name: ");
            out.flush();
            String confName = in.readLine();
            if (confName == null || confName.isBlank()) {
                out.println("Canceled.");
                return;
            }
            Optional<Conference> confOpt = conferenceService.findByName(confName.trim());
            if (confOpt.isEmpty()) {
                out.println("Conference not found: " + confName);
                return;
            }
            Conference conf = confOpt.get();
            if (!membershipService.isMember(currentUser, conf)) {
                out.println("You are not a member of this conference.");
                return;
            }

            List<ThreadEntity> threads = threadService.listThreads(conf);
            if (threads.isEmpty()) {
                out.println("No threads to reply to.");
                return;
            }

            out.println("Available subjects:");
            for (int i = 0; i < threads.size(); i++) {
                out.println(" [" + (i + 1) + "] " + threads.get(i).getTitle());
            }
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

            // Skriv inn svar
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
            out.print("Enter conference name to join: ");
            out.flush();
            String name = in.readLine();
            if (name == null || name.isBlank()) {
                out.println("Join canceled.");
                return;
            }
            Optional<Conference> confOpt = conferenceService.findByName(name.trim());
            if (confOpt.isEmpty()) {
                out.println("Conference not found: " + name);
                return;
            }
            Conference conf = confOpt.get();
            if (!conferenceService.hasAccess(currentUser, conf)) {
                out.println("Access denied: you do not have permission to join this conference.");
                return;
            }
            membershipService.joinConference(currentUser, conf);
            out.println("Joined conference: " + conf.getName());
        } catch (IOException e) {
            out.println("Error reading input.");
        }
    }

    private void leaveConference(PrintWriter out, BufferedReader in) {
        try {
            out.print("Enter conference name to leave: ");
            out.flush();
            String name = in.readLine();
            if (name == null || name.isBlank()) {
                out.println("Leave canceled.");
                return;
            }
            Optional<Conference> confOpt = conferenceService.findByName(name.trim());
            if (confOpt.isEmpty()) {
                out.println("Conference not found: " + name);
                return;
            }
            Conference conf = confOpt.get();
            if (!membershipService.isMember(currentUser, conf)) {
                out.println("You are not a member of this conference.");
                return;
            }
            membershipService.leaveConference(currentUser, conf);
            out.println("Left conference: " + conf.getName());
        } catch (IOException e) {
            out.println("Error reading input.");
        }
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
