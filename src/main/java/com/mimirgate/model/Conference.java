package com.mimirgate.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conferences")
public class Conference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean restricted = false; // Kun SYSOP + COSYSOP

    @Column(nullable = false)
    private boolean vipOnly = false; // VIP + COSYSOP + SYSOP

    @Column(nullable = false)
    private boolean undeletable = false; // Main conference kan ikke slettes

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Cascade: sletter alle threads når konferansen slettes
    @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThreadEntity> threads = new ArrayList<>();

    // Cascade: sletter alle memberships når konferansen slettes
    @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConferenceMembership> memberships = new ArrayList<>();

    public Conference() {}

    public Conference(String name, String description, boolean restricted, boolean vipOnly) {
        this.name = name;
        this.description = description;
        this.restricted = restricted;
        this.vipOnly = vipOnly;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isRestricted() { return restricted; }
    public void setRestricted(boolean restricted) { this.restricted = restricted; }

    public boolean isVipOnly() { return vipOnly; }
    public void setVipOnly(boolean vipOnly) { this.vipOnly = vipOnly; }

    public boolean isUndeletable() { return undeletable; }
    public void setUndeletable(boolean undeletable) { this.undeletable = undeletable; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ThreadEntity> getThreads() { return threads; }

    public List<ConferenceMembership> getMemberships() { return memberships; }
}
