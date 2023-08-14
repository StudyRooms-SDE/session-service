package com.sde.project.session.services;

import com.sde.project.session.models.requests.SessionRequest;
import com.sde.project.session.models.responses.*;
import com.sde.project.session.models.tables.Participation;
import com.sde.project.session.models.tables.Session;
import com.sde.project.session.models.utils.Subject;
import com.sde.project.session.repositories.ParticipationRepository;
import com.sde.project.session.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final ParticipationRepository participationRepository;
    private final RestTemplate restTemplate;

    @Value("${service.room.url}")
    private String roomServiceUrl;

    @Value("${service.file.url}")
    private String fileServiceUrl;


    public SessionService(SessionRepository sessionRepository, ParticipationRepository participationRepository, RestTemplate restTemplate) {
        this.sessionRepository = sessionRepository;
        this.participationRepository = participationRepository;
        this.restTemplate = restTemplate;
    }

    public SessionDetailsResponse getSessionDetails(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new DataRetrievalFailureException("Session not found"));

        Boolean createdByUser = participationRepository.findByUserAndSession(userId, session)
                .map(Participation::getCreated)
                .orElseThrow(() -> new DataRetrievalFailureException("User not participating in this session"));

        RoomDetailsResponse room = restTemplate
                .getForObject(roomServiceUrl + session.getRoomId() + "/details", RoomDetailsResponse.class);

        List<FileResponse> files = Arrays.stream(Objects.requireNonNull(restTemplate
                .getForObject(fileServiceUrl + "?sessionId=" + sessionId, FileResponse[].class))).toList();

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
            if (startTime.isAfter(endTime)) {
                throw new IllegalStateException("Start time is after end time");
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
        OpeningHoursResponse openingHoursResponse = restTemplate.getForObject(roomServiceUrl + sessionRequest.roomId() + "/opening_hours", OpeningHoursResponse.class);
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

    public List<SessionResponse> getSessions(Optional<String> userId) {
        if (userId.isPresent()) {
            return getUserSessions(UUID.fromString(userId.get()));
        } else {
            return sessionRepository.findAll().stream()
                    .map(s -> new SessionResponse(
                            s.getId(),
                            s.getSubject().name(),
                            s.getTopic(),
                            s.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            s.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )).toList();
        }
    }

    private List<SessionResponse> getUserSessions(UUID userId) {
        List<Participation> participations = participationRepository.findAllByUser(userId);
        return participations.stream()
                .map(p -> new SessionResponse(
                        p.getSession().getId(),
                        p.getSession().getSubject().name(),
                        p.getSession().getTopic(),
                        p.getSession().getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        p.getSession().getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )).toList();
    }
}
