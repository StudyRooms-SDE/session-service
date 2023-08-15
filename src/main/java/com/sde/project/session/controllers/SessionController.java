package com.sde.project.session.controllers;

import com.sde.project.session.models.requests.SessionRequest;
import com.sde.project.session.models.responses.SessionDetailsResponse;
import com.sde.project.session.models.responses.SessionResponse;
import com.sde.project.session.models.utils.Subject;
import com.sde.project.session.services.SessionService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<SessionResponse> getSessions(@RequestParam(name="userId") Optional<String> userId, @RequestParam Optional<String> subject) {
        return sessionService.getSessions(userId, subject);
    }

    @GetMapping(path = "/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public SessionDetailsResponse getSessionDetails(@RequestParam("userId") String userId, @PathVariable String sessionId) {
        return sessionService.getSessionDetails(UUID.fromString(userId), UUID.fromString(sessionId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createSession(@RequestBody SessionRequest sessionRequest) {
        sessionService.createSession(sessionRequest);
    }

    @PostMapping(path = "/{sessionId}/participate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void participate(@RequestParam("userId") String userId, @PathVariable String sessionId) {
        sessionService.joinSession(UUID.fromString(userId), UUID.fromString(sessionId));
    }

    @DeleteMapping(path = "/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@RequestParam("userId") String userId, @PathVariable String sessionId) {
        sessionService.deleteSession(UUID.fromString(userId), UUID.fromString(sessionId));
    }

    @PostMapping(path = "/{sessionId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveSession(@RequestParam("userId") String userId, @PathVariable String sessionId) {
        sessionService.leaveSession(UUID.fromString(userId), UUID.fromString(sessionId));
    }

    @GetMapping(path = "/subjects", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<String> getSubjects() {
        return Arrays.stream(Subject.values()).map(Subject::name).toList();
    }
}
