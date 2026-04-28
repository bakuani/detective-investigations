package com.detective.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthResetRequest {
    @NotBlank(message = "Username must not be blank")
    private String username;
    
    @NotBlank(message = "New password must not be blank")
    private String newPassword;
}
