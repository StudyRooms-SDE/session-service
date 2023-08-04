package com.sde.project.session.models.tables;

import jakarta.persistence.*;

import java.util.UUID;

@Table(name = "participation")
@Entity(name = "participation")
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "session_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Session session;

    @Column(name = "user_id", nullable = false)
    private UUID user;

    @Column(name = "created", nullable = false)
    private Boolean created;
    public Participation() {
    }

    public Participation(Session session, UUID user, Boolean created) {
        this.session = session;
        this.user = user;
        this.created = created;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }
}
