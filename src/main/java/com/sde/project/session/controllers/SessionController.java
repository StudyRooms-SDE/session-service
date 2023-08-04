package com.sde.project.session.controllers;

import com.sde.project.session.models.responses.SessionDetailsResponse;
import com.sde.project.session.models.responses.SessionResponse;
import com.sde.project.session.services.SessionService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping(produces = "application/json")
    public List<SessionResponse> getUserSessions(@RequestParam("userId") String userId) {
        return sessionService.getUserSessions(UUID.fromString(userId)).entrySet().stream()
                .map(e -> new SessionResponse(
                        e.getKey().getId(),
                        e.getValue(),
                        e.getKey().getStartTime().toString(),
                        e.getKey().getEndTime().toString()))
                .toList();
    }

    @GetMapping(path = "/{sessionId}", produces = "application/json")
    public SessionDetailsResponse getSessionDetails(@RequestParam("userId") String userId, @PathVariable String sessionId) {
        return sessionService.getSessionDetails(UUID.fromString(userId), UUID.fromString(sessionId));
    }
}
