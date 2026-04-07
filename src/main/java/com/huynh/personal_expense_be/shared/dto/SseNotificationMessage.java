package com.huynh.personal_expense_be.shared.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SseNotificationMessage{

    private String id;
    private SseNotificationEventType eventType; 
    private String message; 
    private String description;
    private Instant timestamp;
    
}
