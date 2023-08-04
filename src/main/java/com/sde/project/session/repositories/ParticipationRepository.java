package com.sde.project.session.repositories;

import com.sde.project.session.models.tables.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, UUID> {
    List<Participation> findAllByUser(UUID userId);
}
