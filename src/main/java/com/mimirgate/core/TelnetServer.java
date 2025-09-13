package com.mimirgate.core;

import com.mimirgate.core.util.MenuLoader;
import com.mimirgate.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TelnetServer {

    @Value("${telnet.port:2323}")
    private int port;

    private ServerSocket serverSocket;
    private ExecutorService clientPool;
    private boolean running = true;
    private Map<String, String> menuTexts40;
    private Map<String, String> menuTexts80;

    private final UserService userService;
    private final WallService wallService;
    private final ConferenceService conferenceService;
    private final ConferenceMembershipService conferenceMembershipService;
    private final ThreadService threadService;
    private final PostService postService;

    public TelnetServer(UserService userService,
                        WallService wallService,
                        ConferenceService conferenceService,
                        ConferenceMembershipService conferenceMembershipService,
                        ThreadService threadService,
                        PostService postService) {
        this.userService = userService;
        this.wallService = wallService;
        this.conferenceService = conferenceService;
        this.conferenceMembershipService = conferenceMembershipService;
        this.threadService = threadService;
        this.postService = postService;
    }

    @PostConstruct
    public void start() {
        // Bruk MenuLoader i stedet for intern metode
        this.menuTexts40 = MenuLoader.loadMenus(40);
        this.menuTexts80 = MenuLoader.loadMenus(80);

        clientPool = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Telnet server started on port " + port);

            Thread serverThread = new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter out = new PrintWriter(
                                new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

                        clientPool.submit(new SessionHandler(
                                clientSocket, in, out, menuTexts40, menuTexts80,
                                userService, wallService, conferenceService,
                                threadService, postService));

                    } catch (IOException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            serverThread.setDaemon(true);
            serverThread.start();

        } catch (IOException e) {
            throw new RuntimeException("Unable to start Telnet server on port " + port, e);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (clientPool != null) {
            clientPool.shutdown();
        }
        System.out.println("Telnet server stopped.");
    }
}
