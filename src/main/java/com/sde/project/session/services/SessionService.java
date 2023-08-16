package com.sde.project.session.services;

import com.sde.project.session.models.requests.SessionRequest;
import com.sde.project.session.models.responses.*;
import com.sde.project.session.models.tables.Participation;
import com.sde.project.session.models.tables.Session;
import com.sde.project.session.models.utils.Subject;
import com.sde.project.session.repositories.ParticipationRepository;
import com.sde.project.session.repositories.SessionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final ParticipationRepository participationRepository;
    private final RestTemplate restTemplate;
    private HttpHeaders headers;

    @Value("${x.auth.secret}")
    private String xAuthSecretKey;

    @Value("${service.room.url}")
    private String roomServiceUrl;

    @Value("${service.file.url}")
    private String fileServiceUrl;


    public SessionService(SessionRepository sessionRepository, ParticipationRepository participationRepository, RestTemplate restTemplate) {
        this.sessionRepository = sessionRepository;
        this.participationRepository = participationRepository;
        this.restTemplate = restTemplate;
        this.headers = new HttpHeaders();
    }

    @PostConstruct
    private void initializeToken() {
        headers.add("x-auth-secret-key", xAuthSecretKey);
    }

    public List<SessionResponse> getSessions(Optional<String> userId, Optional<String> subject) {
        List<Session> sessions;
        if (userId.isPresent()) {
            sessions = participationRepository.findAllByUser(UUID.fromString(userId.get()))
                    .stream()
                    .map(Participation::getSession)
                    .toList();
        } else if (subject.isPresent()) {
            sessions = sessionRepository.findBySubject(Subject.valueOf(subject.get()));
        } else {
            sessions = sessionRepository.findAll();
        }
        return sessions.stream()
                .map(s -> {
                    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
                    String room = restTemplate.exchange(roomServiceUrl + "/" + s.getRoomId(), HttpMethod.GET, requestEntity, RoomResponse.class).getBody().building();
                    if (userId.isPresent()) {
                        Optional<Object> createdByUser = participationRepository.findByUserAndSession(UUID.fromString(userId.get()), s)
                                .map(Participation::getCreated);
                        return new SessionResponse(
                                s.getId(),
                                room,
                                s.getSubject().name(),
                                s.getTopic(),
                                s.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                s.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                createdByUser.isPresent());
                    }
                    return new SessionResponse(
                            s.getId(),
                            room,
                            s.getSubject().name(),
                            s.getTopic(),
                            s.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            s.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            false);
                })
                .toList();
    }

    public SessionDetailsResponse getSessionDetails(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new DataRetrievalFailureException("Session not found"));

        Boolean createdByUser = participationRepository.findByUserAndSession(userId, session)
                .map(Participation::getCreated)
                .orElseThrow(() -> new DataRetrievalFailureException("User not participating in this session"));

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        RoomDetailsResponse room = restTemplate.exchange(roomServiceUrl + "/" + session.getRoomId() + "/details", HttpMethod.GET, requestEntity, RoomDetailsResponse.class).getBody();

        List<FileResponse> files = Arrays.stream(Objects.requireNonNull(restTemplate.exchange(fileServiceUrl + "?sessionId=" + sessionId, HttpMethod.GET, requestEntity, FileResponse[].class).getBody())).toList();

        return new SessionDetailsResponse(
                session.getId(),
                session.getSubject().name(),
                session.getTopic(),
                session.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                session.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                createdByUser,
                room,
                files
        );

    }

    public void createSession(SessionRequest sessionRequest) {
        if (checkRoomAvailable(sessionRequest) && checkUserAvailable(sessionRequest) && checkRoomOpen(sessionRequest)) {
            LocalDateTime startTime = LocalDateTime.parse(sessionRequest.startTime());
            LocalDateTime endTime = LocalDateTime.parse(sessionRequest.endTime());
            LocalDateTime now = LocalDateTime.now();
            if (startTime.isAfter(endTime)) {
                throw new IllegalStateException("Start time is after end time");
            }
            if (startTime.isBefore(now)) {
                throw new IllegalStateException("Start time is in the past");
            }
            Session session = new Session(
                    Subject.valueOf(sessionRequest.subject()),
                    sessionRequest.topic().orElse(null),
                    startTime,
                    endTime,
                    sessionRequest.roomId()
            );
            sessionRepository.save(session);
            participationRepository.save(new Participation(session, sessionRequest.userId(), true));
        }


    }

    public void joinSession(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new DataRetrievalFailureException("Session not found"));
        participationRepository.findByUserAndSession(userId, session)
                .ifPresentOrElse(p -> {
                    throw new IllegalStateException("User is already participating in this session");
                }, () -> {
                    participationRepository.save(new Participation(session, userId, false));
                });
    }

    @Transactional
    public void deleteSession(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new DataRetrievalFailureException("Session not found"));

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        List<FileResponse> sessionFiles = Arrays.stream(Objects.requireNonNull(restTemplate.exchange(fileServiceUrl + "?sessionId=" + sessionId, HttpMethod.GET, requestEntity, FileResponse[].class).getBody())).toList();

        participationRepository.findByUserAndSession(userId, session)
                .ifPresentOrElse(p -> {
                    if (p.getCreated()) {
                        participationRepository.deleteABySession(session);
                        sessionRepository.delete(session);
                    } else {
                        throw new IllegalStateException("User is not the creator of this session");
                    }
                }, () -> {
                    throw new IllegalStateException("User is not participating in this session");
                });

        // delete session files as well
        restTemplate.exchange(fileServiceUrl + "?sessionId=" + sessionId, HttpMethod.DELETE, requestEntity, Void.class);

    }

    public void leaveSession(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new DataRetrievalFailureException("Session not found"));
        participationRepository.findByUserAndSession(userId, session)
                .ifPresentOrElse(p -> {
                    if (p.getCreated()) {
                        throw new IllegalStateException("User is the creator of this session");
                    }
                    participationRepository.delete(p);
                }, () -> {
                    throw new IllegalStateException("User is not participating in this session");
                });
    }
    private Boolean checkRoomAvailable(SessionRequest sessionRequest) {
        sessionRepository.findByRoomId(sessionRequest.roomId())
                .stream()
                .filter(s -> s.getStartTime().isBefore(LocalDateTime.parse(sessionRequest.endTime())) && s.getEndTime().isAfter(LocalDateTime.parse(sessionRequest.startTime())))
                .findAny()
                .ifPresent(s -> {
                    throw new IllegalStateException("Room is already booked at the same time");
                });
        return true;
    }

    private Boolean checkRoomOpen(SessionRequest sessionRequest) {
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        OpeningHoursResponse openingHoursResponse = restTemplate.exchange(roomServiceUrl + "/" + sessionRequest.roomId() + "/opening_hours", HttpMethod.GET, requestEntity, OpeningHoursResponse.class).getBody();

        LocalDateTime requestStartTime = LocalDateTime.parse(sessionRequest.startTime());
        LocalDateTime requestEndTime = LocalDateTime.parse(sessionRequest.endTime());

        openingHoursResponse.openingHours().stream()
                .filter(o -> o.dayOfWeek().equals(requestStartTime.getDayOfWeek()))
                .findAny()
                .ifPresentOrElse(o -> {
                    if (requestStartTime.toLocalTime().isBefore(o.startTime()) || requestEndTime.toLocalTime().isAfter(o.endTime())) {
                        throw new IllegalStateException("Room is not open at the requested time");
                    }
                }, () -> {
                    throw new DataIntegrityViolationException("Room is not open in the requested day");
                });
        return true;
    }

    private Boolean checkUserAvailable(SessionRequest sessionRequest) {
        participationRepository.findAllByUser(sessionRequest.userId())
                .stream()
                .filter(p -> p.getSession().getStartTime().isBefore(LocalDateTime.parse(sessionRequest.endTime())) && p.getSession().getEndTime().isAfter(LocalDateTime.parse(sessionRequest.startTime())))
                .findAny()
                .ifPresent(p -> {
                    throw new IllegalStateException("User is already participating in another session at the same time");
                });
        return true;
    }
}
