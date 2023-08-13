package com.sde.project.session.models.responses;

import java.util.UUID;

public record SessionResponse(UUID sessionId, String building, String subject, String startTime, String endTime) {
}
