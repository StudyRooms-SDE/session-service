package com.sde.project.session.models.requests;

import java.util.Optional;
import java.util.UUID;

public record SessionRequest(UUID userId, UUID roomId, String subject, Optional<String> topic, String startTime, String endTime) {
}
