package com.mimirgate.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conference_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "conference_id"}))
public class ConferenceMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conference_id")
    private Conference conference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public ConferenceMembership() {}

    public ConferenceMembership(User user, Conference conference) {
        this.user = user;
        this.conference = conference;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Conference getConference() { return conference; }
    public void setConference(Conference conference) { this.conference = conference; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
