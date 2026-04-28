package com.detective.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolutionSubmitRequest {
    @NotBlank
    private String suspect;
    
    @NotBlank
    private String motive;
    
    @NotBlank
    private String method;
    
    private String justification;
}
