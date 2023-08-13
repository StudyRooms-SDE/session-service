package com.sde.project.session.models.responses;

import java.util.List;
import java.util.UUID;

public record SessionDetailsResponse(UUID id, String subject, String topic, String startTime, String endTime, RoomDetailsResponse room, List<FileResponse> files) {
}

