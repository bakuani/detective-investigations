package com.detective.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatSendMessageRequest {
    @NotBlank(message = "Message content must not be blank")
    private String content;
}
