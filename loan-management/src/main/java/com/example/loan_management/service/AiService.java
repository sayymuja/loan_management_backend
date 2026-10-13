        package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.AiChatRequest;
import com.example.loan_management.dto.AiChatResponse;
import com.example.loan_management.dto.WomenDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final CurrentUserService currentUserService;
    private final WomenService womenService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AiService(
            CurrentUserService currentUserService,
            WomenService womenService,
            ObjectMapper objectMapper) {

        this.currentUserService = currentUserService;
        this.womenService = womenService;
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public AiChatResponse chat(String question) {

        String cmrcName = currentUserService.getCurrentCmrcName();

        String normalizedQuestion = question.toLowerCase().trim();

        String dataContext = "";

        // Handle women list/count questions using actual backend data.
        if (isWomenQuestion(normalizedQuestion)) {

            List<WomenDto> women = womenService.getAll();

            if (isWomenCountQuestion(normalizedQuestion)) {

                long totalWomen = women.size();

                long activeWomen = women.stream()
                        .filter(w -> w.getStatus() != null)
                        .filter(w -> "ACTIVE".equalsIgnoreCase(
                                w.getStatus().trim()))
                        .count();

                dataContext = """
                        Verified database results for the logged-in user's CMRC:
                        Total registered women: %d
                        Women with status ACTIVE: %d

                        These figures were calculated by the backend.
                        Do not change or invent these numbers.
                        """.formatted(totalWomen, activeWomen);

            } else {

                String womenList = women.stream()
                        .map(w -> {
                            String name = safe(w.getWomanName());
                            String group = safe(w.getGroupName());
                            String village = safe(w.getVillageName());
                            String status = safe(w.getStatus());

                            return "- Name: " + name
                                    + ", Group: " + group
                                    + ", Village: " + village
                                    + ", Status: " + status;
                        })
                        .collect(Collectors.joining("\n"));

                dataContext = women.isEmpty()
                        ? "Verified database result: No women records found for this CMRC."
                        : """
                          Verified women records for the logged-in user's CMRC:
                          Total records: %d
                          %s

                          Only report the records shown above.
                          Do not invent names or details.
                          """.formatted(women.size(), womenList);
            }
        }

        String prompt = """
                You are the local AI assistant for a Loan Management System.

                Logged-in user's CMRC: %s

                User question:
                %s

                Verified backend data:
                %s

                Rules:
                1. Answer clearly in the user's language.
                2. Use verified backend data when it is provided.
                3. Never invent names, counts, loan amounts or financial figures.
                4. If the required database data is not provided, say that this
                   information is not connected yet.
                5. Do not claim to perform transactions.
                6. Do not expose mobile numbers or addresses.
                """.formatted(cmrcName, question, dataContext);

        try {
            Map<String, Object> payload = Map.of(
                    "model", "qwen2.5:3b",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    ),
                    "stream", false
            );

            String requestBody =
                    objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/chat"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Ollama returned HTTP status "
                                + response.statusCode()
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            String answer = root.path("message")
                    .path("content")
                    .asText("");

            if (answer.isBlank()) {
                throw new IllegalStateException(
                        "Ollama returned an empty answer"
                );
            }

            return new AiChatResponse(answer, cmrcName);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "AI request was interrupted", e
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot connect to local Ollama. "
                            + "Check that Ollama is running.", e
            );
        }
    }

    private boolean isWomenQuestion(String question) {
        return question.contains("women")
                || question.contains("woman")
                || question.contains("महिला")
                || question.contains("महिलाओं")
                || question.contains("mahila")
                || question.contains("mahilaon")
                || question.contains("aurat")
                || question.contains("auratein");
    }

    private boolean isWomenCountQuestion(String question) {
        return question.contains("how many")
                || question.contains("total")
                || question.contains("count")
                || question.contains("number of")
                || question.contains("kitni")
                || question.contains("kitne")
                || question.contains("kul")
                || question.contains("कितनी")
                || question.contains("कितने")
                || question.contains("कुल")
                || question.contains("संख्या");
    }

    private String safe(String value) {
        return value == null || value.isBlank()
                ? "Not available"
                : value.trim();
    }
}
