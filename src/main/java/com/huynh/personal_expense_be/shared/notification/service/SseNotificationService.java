package com.huynh.personal_expense_be.shared.notification.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SseNotificationService {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); 
        
        this.emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("CONNECT").data("Connected successfully"));
        } catch (Exception e) {
            log.info("Client disconnected immediately upon SSE connection: {} - {}", userId, e.getMessage());
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    public void sendNotification(String userId, SseNotificationMessage message) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(message.getEventType().name())
                            .data(message));
                } catch (Exception e) {
                    log.info("Client disconnected or error sending SSE to user: {} - {}", userId, e.getMessage());
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            log.info("Removing SSE emitter for user: {}", userId);
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
}
