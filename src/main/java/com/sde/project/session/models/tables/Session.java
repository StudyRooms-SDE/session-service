package com.sde.project.session.models.tables;

import com.sde.project.session.models.utils.Subject;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity(name = "sessions")
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject", nullable = false)
    private Subject subject;

    @Column(name = "topic", nullable = false)
    private  String topic;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private  LocalDateTime endTime;


    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    public Session() {}

    public Session(Subject subject, String topic, LocalDateTime startTime, LocalDateTime endTime,  UUID roomId) {
        this.subject = subject;
        this.topic = topic;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomId = roomId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }
}
