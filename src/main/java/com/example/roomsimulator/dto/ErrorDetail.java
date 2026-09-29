package com.example.roomsimulator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorDetail(
        String type,
        String message,
        String description,
        List<String> possibleCauses,
        List<String> recommendedAction) {
}
