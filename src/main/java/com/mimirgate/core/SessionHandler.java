package com.mimirgate.core;

import com.mimirgate.core.menus.*;
import com.mimirgate.core.util.WallRenderer;
import com.mimirgate.model.LoginResult;
import com.mimirgate.model.User;
import com.mimirgate.model.WallMessage;
import com.mimirgate.service.UserService;
import com.mimirgate.service.WallService;

import java.io.*;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.mimirgate.core.util.WallRenderer.renderWall;

public class SessionHandler implements Runnable {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Map<String, String> menuTexts40;
    private final Map<String, String> menuTexts80;
    private final Map<String, MenuHandler> menus = new HashMap<>();
    private int terminalWidth = 80;
    private static final List<String> wisdoms = new ArrayList<>();
    private static final Random random = new Random();
    private String currentMenu = "MAIN";

    private final UserService userService;
    private final WallService wallService;
    private User currentUser;

    public SessionHandler(Socket socket, BufferedReader in, PrintWriter out,
                          Map<String,String> menuTexts40, Map<String,String> menuTexts80,
                          UserService userService, WallService wallService) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.menuTexts40 = menuTexts40;
        this.menuTexts80 = menuTexts80;
        this.userService = userService;
        this.wallService = wallService;
        loadWisdoms();
        // Merk: initMenus() kjøres først etter login når vi kjenner bruker + width
    }

    private void loadWisdoms() {
        if (!wisdoms.isEmpty()) return;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("wisdoms.txt")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) wisdoms.add(line.trim());
                    }
                }
            }
        } catch (IOException ignored) {}
        if (wisdoms.isEmpty()) wisdoms.add("The Wisdom of the ancients, today!");
    }

    private String getRandomWisdom() {
        return wisdoms.get(random.nextInt(wisdoms.size()));
    }

    private Map<String, String> getActiveMenus() {
        return terminalWidth == 40 ? menuTexts40 : menuTexts80;
    }

    private void initMenus() {
        Map<String, String> activeMenus = getActiveMenus();
        menus.clear();
        menus.put("MAIN",   new MainMenuHandler(activeMenus.get("MAIN")));
        menus.put("CONFIG", new ConfigMenuHandler(activeMenus.get("CONFIG")));
        menus.put("SYSOP",  new SysopMenuHandler(activeMenus.get("SYSOP")));
        menus.put("PM",     new PmMenuHandler(activeMenus.get("PM")));
        // Viktig: WallMenuHandler trenger service + bruker + width
        String username = (currentUser != null) ? currentUser.getUsername() : "guest";
        menus.put("WALL",   new WallMenuHandler(activeMenus.get("WALL"), wallService, username, terminalWidth));
        menus.put("FILE",   new FileMenuHandler(activeMenus.get("FILE")));
    }

    private void printMenu() {
        out.println();
        if (terminalWidth == 80) {
            out.println("MIMIRGATE BBS : " + getRandomWisdom());
        } else {
            out.println(getRandomWisdom());
        }
        out.println(getActiveMenus().get(currentMenu));
    }

    private void printPrompt() {
        MenuHandler h = menus.get(currentMenu);
        String prompt = (h != null) ? h.getPrompt() : (currentMenu + " (? for menu) > ");
        out.print(prompt);
        out.flush();
    }

    @Override
    public void run() {
        try {
            // 1) Login
            LoginHandler loginHandler = new LoginHandler(userService, menuTexts40, menuTexts80);
            Optional<LoginResult> loginResultOpt = loginHandler.handleLogin(out, in);
            if (loginResultOpt.isEmpty()) return;

            LoginResult loginResult = loginResultOpt.get();
            if (loginResult.getStatus() == LoginResult.LoginStatus.DISCONNECT) return;
            this.terminalWidth = loginResult.getTerminalWidth();
            this.currentUser   = loginResult.getUser();

            // Menyene må initialiseres ETTER at bruker/width er kjent
            initMenus();

            // 2) Vis Wall “velkomst” først
            showWallPreviewAndWait();

            // 3) Vanlig menyløype
            printMenu();
            printPrompt();

            String command;
            boolean running = true;

            while (running && (command = in.readLine()) != null) {
                command = command.trim();
                if (command.isEmpty()) { printPrompt(); continue; }

                // Globale
                if ("40".equalsIgnoreCase(command)) {
                    setTerminalWidth(40);
                    initMenus();
                    printMenu(); printPrompt();
                    continue;
                }
                if ("80".equalsIgnoreCase(command)) {
                    setTerminalWidth(80);
                    initMenus();
                    printMenu(); printPrompt();
                    continue;
                }
                if ("?".equalsIgnoreCase(command)) {
                    printMenu(); printPrompt();
                    continue;
                }

                // Deleger til aktiv meny
                MenuHandler handler = menus.get(currentMenu);
                if (handler == null) {
                    out.println("\n[Internal] No handler for menu: " + currentMenu);
                    printPrompt();
                    continue;
                }

                MenuNav nav = handler.handleCommand(command, out, in);

                switch (nav) {
                    case DISCONNECT: out.println("Goodbye!"); running = false; break;
                    case MAIN:   currentMenu = "MAIN";   printMenu(); break;
                    case CONFIG: currentMenu = "CONFIG"; printMenu(); break;
                    case SYSOP:  currentMenu = "SYSOP";  printMenu(); break;
                    case PM:     currentMenu = "PM";     printMenu(); break;
                    case WALL:   currentMenu = "WALL";   printMenu(); break;
                    case FILE:   currentMenu = "FILE";   printMenu(); break;
                    case STAY:
                    default: break;
                }

                if (running) printPrompt();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public void setTerminalWidth(int width) {
        if (width == 40 || width == 80) {
            this.terminalWidth = width;
        }
    }

    private void showWallPreviewAndWait() throws IOException {
        List<WallMessage> msgs = wallService.getMessagesForWidth(terminalWidth);
        renderWall(out, in, msgs, terminalWidth);
    }

    // Enkle hjelpere (bruk dine hvis du har fra før)
    private String currentUserLine(String user, String ts) {
        String s = user + " — " + ts;
        return s.length() > terminalWidth ? s.substring(0, terminalWidth) : s;
    }

    private List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(i + width, text.length());
            lines.add(text.substring(i, end));
            i = end;
        }
        return lines;
    }
}
