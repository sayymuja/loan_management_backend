
package com.example.loan_management.controller;

import com.example.loan_management.dto.AiChatRequest;
import com.example.loan_management.dto.AiChatResponse;
import com.example.loan_management.service.AiService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request) {

        AiChatResponse response =
                aiService.chat(request.question());

        return ResponseEntity.ok(response);
    }
}
