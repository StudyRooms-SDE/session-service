package com.sde.project.session.repositories;

import com.sde.project.session.models.tables.Participation;
import com.sde.project.session.models.tables.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, UUID> {
    @Query("SELECT p FROM participation p WHERE p.user = :userId")
    List<Participation> findAllByUser(UUID userId);

    @Query("SELECT p FROM participation p WHERE p.user = :userId AND p.session = :session")
    Optional<Participation> findByUserAndSession(UUID userId, Session session);
}
