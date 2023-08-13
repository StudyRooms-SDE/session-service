package com.sde.project.session.services;

import com.sde.project.session.models.responses.FileResponse;
import com.sde.project.session.models.responses.RoomDetailsResponse;
import com.sde.project.session.models.responses.RoomResponse;
import com.sde.project.session.models.responses.SessionDetailsResponse;
import com.sde.project.session.models.tables.Participation;
import com.sde.project.session.models.tables.Session;
import com.sde.project.session.repositories.ParticipationRepository;
import com.sde.project.session.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final ParticipationRepository participationRepository;
    private final RestTemplate restTemplate;

    @Value("${service.room.url}")
    private String roomServiceUrl ;

    @Value("${service.file.url}")
    private String fileServiceUrl;


    public SessionService(SessionRepository sessionRepository, ParticipationRepository participationRepository, RestTemplate restTemplate) {
        this.sessionRepository = sessionRepository;
        this.participationRepository = participationRepository;
        this.restTemplate = restTemplate;
    }

    public Map<Session, String> getUserSessions(UUID userId) {
        List<Participation> participations = participationRepository.findAllByUser(userId);
        Map<Session, String> response = participations.stream()
                .map(p -> {
                    Session session = p.getSession();
                    String roomBuilding = restTemplate
                            .getForObject(roomServiceUrl + session.getRoomId(), RoomResponse.class)
                            .building();
            return new HashMap.SimpleEntry<>(session, roomBuilding);
        }).collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
        return response;
    }

    public SessionDetailsResponse getSessionDetails(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new DataRetrievalFailureException("Session not found"));

        participationRepository.findAllByUser(userId).stream()
                .filter(p -> p.getSession().getId().equals(sessionId))
                .findAny()
                .orElseThrow(() -> new DataRetrievalFailureException("User not participating in this session"));

        RoomDetailsResponse room = restTemplate
                .getForObject(roomServiceUrl + session.getRoomId() + "/details", RoomDetailsResponse.class);
        List<FileResponse> files = Arrays.stream(Objects.requireNonNull(restTemplate
                .getForObject(fileServiceUrl + "?sessionId=" + sessionId, FileResponse[].class))).toList();
        return new SessionDetailsResponse(
                session.getId(),
                session.getTopic(),
                session.getStartTime().format(DateTimeFormatter.RFC_1123_DATE_TIME),
                session.getEndTime().format(DateTimeFormatter.RFC_1123_DATE_TIME),
                room,
                files
        );

    }
}
