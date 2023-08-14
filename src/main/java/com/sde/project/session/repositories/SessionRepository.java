package com.sde.project.session.repositories;

import com.sde.project.session.models.tables.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    @Query("SELECT s FROM sessions s WHERE s.roomId = :roomId")
    List<Session> findByRoomId(UUID roomId);
}
