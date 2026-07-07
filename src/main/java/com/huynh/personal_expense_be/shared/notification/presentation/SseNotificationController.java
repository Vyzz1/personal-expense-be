package com.huynh.personal_expense_be.shared.notification.presentation;

import java.security.Principal;

import com.huynh.personal_expense_be.shared.notification.service.SseNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Server-Sent Events for real-time budget threshold alerts")
@SecurityRequirement(name = "bearerAuth")
public class SseNotificationController {

    private final SseNotificationService sseNotificationService;

    @Operation(summary = "Subscribe to real-time notifications", description = "Opens an SSE stream. The server pushes events when a budget threshold is exceeded.")
    @ApiResponse(responseCode = "200", description = "SSE stream opened")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Principal principal, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        String userId = principal.getName();
        return sseNotificationService.subscribe(userId);
    }
}
