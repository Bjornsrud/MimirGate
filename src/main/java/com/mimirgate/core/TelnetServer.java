package com.mimirgate.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TelnetServer {

    @Value("${telnet.port:2323}")
    private int port;

    private ServerSocket serverSocket;
    private ExecutorService clientPool;
    private boolean running = true;

    @PostConstruct
    public void start() {
        clientPool = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Telnet server started on port " + port);

            Thread serverThread = new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        clientPool.submit(new SessionHandler(clientSocket));
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
