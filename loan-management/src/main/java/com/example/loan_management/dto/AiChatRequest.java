
package com.example.loan_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank(message = "Question cannot be empty")
        @Size(max = 2000, message = "Question cannot exceed 2000 characters")
        String question
) {
}
