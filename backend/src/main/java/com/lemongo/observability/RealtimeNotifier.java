package com.lemongo.observability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeNotifier {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;

    public void register(WebSocketSession session) {
        sessions.add(session);
    }

    public void unregister(WebSocketSession session) {
        sessions.remove(session);
    }

    public void publish(String type, Object data) {
        if (sessions.isEmpty()) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(
                    new RealtimeEnvelope(type, data, LocalDateTime.now()));
            for (WebSocketSession session : sessions) {
                send(session, payload);
            }
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize realtime event type={}", type, ex);
        }
    }

    private void send(WebSocketSession session, String payload) {
        if (!session.isOpen()) {
            sessions.remove(session);
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException ex) {
            sessions.remove(session);
            try {
                session.close();
            } catch (IOException closeError) {
                log.debug("Failed to close websocket session {}", session.getId(), closeError);
            }
        }
    }

    public int sessionCount() {
        return sessions.size();
    }

    private record RealtimeEnvelope(
            String type,
            Object data,
            LocalDateTime timestamp) {
    }
}
