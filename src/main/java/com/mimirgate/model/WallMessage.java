package com.mimirgate.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wall_messages")
public class WallMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public WallMessage() {}

    public WallMessage(String username, String content) {
        this.username = username;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
