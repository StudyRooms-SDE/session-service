package com.sde.project.session.controllers;

import com.sde.project.session.models.requests.SessionRequest;
import com.sde.project.session.models.responses.SessionDetailsResponse;
import com.sde.project.session.models.responses.SessionResponse;
import com.sde.project.session.services.SessionService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping(produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<SessionResponse> getSessions(@RequestParam(name="userId") Optional<String> userId){
        return sessionService.getSessions(userId);
    }

    @GetMapping(path = "/{sessionId}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public SessionDetailsResponse getSessionDetails(@RequestParam("userId") String userId, @PathVariable String sessionId) {
        return sessionService.getSessionDetails(UUID.fromString(userId), UUID.fromString(sessionId));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSession(@RequestBody SessionRequest sessionRequest) {
        sessionService.createSession(sessionRequest);
    }

    @PostMapping(path = "/{sessionId}/participate", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void participate(@RequestParam("userId") String userId, @PathVariable String sessionId) {
        sessionService.joinSession(UUID.fromString(userId), UUID.fromString(sessionId));
    }
}
