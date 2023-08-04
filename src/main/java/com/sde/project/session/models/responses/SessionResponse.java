package com.sde.project.session.models.responses;

import java.util.UUID;

public record SessionResponse(UUID sessionId, String room, String startTime, String endTime) {
}
