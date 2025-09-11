package com.mimirgate.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
    private Map<String, String> menuTexts40 = new HashMap<>();
    private Map<String, String> menuTexts80 = new HashMap<>();

    @PostConstruct
    public void start() {
        loadMenuTexts();

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

                        clientPool.submit(new SessionHandler(clientSocket, in, out, menuTexts40, menuTexts80));

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

    private void loadMenuTexts() {
        String[] menuFiles = { "mainmenu.txt", "configmenu.txt", "sysopmenu.txt", "pmenu.txt", "wallmenu.txt", "filemenu.txt" };
        String[] menuKeys = { "MAIN", "CONFIG", "SYSOP", "PM", "WALL", "FILE" };

        for (int i = 0; i < menuFiles.length; i++) {
            menuTexts40.put(menuKeys[i], loadResourceFile("menus/40/" + menuFiles[i]));
            menuTexts80.put(menuKeys[i], loadResourceFile("menus/80/" + menuFiles[i]));
        }
    }

    private String loadResourceFile(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Menu file " + path + " not found.";
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
