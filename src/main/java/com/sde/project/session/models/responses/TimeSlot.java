package com.sde.project.session.models.responses;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record TimeSlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) { }
