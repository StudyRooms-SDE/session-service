package com.sde.project.session.models.responses;

import java.util.List;

public record OpeningHoursResponse(RoomResponse room, List<TimeSlot> openingHours) {
}
