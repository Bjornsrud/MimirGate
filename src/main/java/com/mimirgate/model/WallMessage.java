package com.mimirgate.model;

import java.time.LocalDateTime;

public class WallMessage {
    private final String username;
    private final String content;
    private final LocalDateTime timestamp;

    public WallMessage(String username, String content) {
        this.username = username;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + username + ": " + content;
    }
}
