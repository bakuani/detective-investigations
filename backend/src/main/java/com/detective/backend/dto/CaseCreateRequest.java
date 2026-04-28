package com.detective.backend.dto;

import com.detective.backend.domain.Mode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CaseCreateRequest {
    @NotNull
    private Mode mode;
    
    @NotBlank
    private String theme;
    
    @NotBlank
    private String difficulty;

    private Integer cluesCount;

    private Integer deadlineDays;
}
