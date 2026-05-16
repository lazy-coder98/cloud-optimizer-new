package com.cloud.optimizer.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.cloud.optimizer.model.AiAdviceRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GeminiAdvisorService {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    public GeminiAdvisorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateAdvice(AiAdviceRequest adviceRequest) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini API key is not configured. Add GEMINI_API_KEY in Render."
            );
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(GEMINI_URL, model)))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(adviceRequest)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Gemini request failed with status " + response.statusCode()
                );
            }

            return extractText(response.body());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not call Gemini API", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini request was interrupted", ex);
        }
    }

    private String buildRequestBody(AiAdviceRequest request) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode content = objectMapper.createObjectNode();
        ObjectNode part = objectMapper.createObjectNode();

        part.put("text", buildPrompt(request));
        content.putArray("parts").add(part);
        root.putArray("contents").add(content);

        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.35);
        generationConfig.put("maxOutputTokens", 500);
        root.set("generationConfig", generationConfig);

        return objectMapper.writeValueAsString(root);
    }

    private String buildPrompt(AiAdviceRequest request) {
        String userQuestion = request.getQuestion() == null || request.getQuestion().isBlank()
                ? "Explain this recommendation and give the top next steps."
                : request.getQuestion().trim();

        return """
                You are an AI Cloud Cost Advisor inside a cloud optimizer app.
                Give concise, practical advice. Do not invent live pricing. Use the provided rule-based result as the source of truth.
                Structure the answer with: Summary, Why it matters, Next steps.

                Cloud context:
                Provider: %s
                Workload type: %s
                Monthly cost: %s
                CPU usage: %s%%
                Memory usage: %s%%
                Storage usage: %s%%
                Rule recommendation: %s
                Severity: %s
                Estimated saving percent: %s%%
                Estimated monthly saving amount: %s
                Rule rationale: %s

                User question: %s
                """.formatted(
                valueOrUnknown(request.getProvider()),
                valueOrUnknown(request.getWorkloadType()),
                valueOrUnknown(request.getMonthlyCost()),
                valueOrUnknown(request.getCpuUsage()),
                valueOrUnknown(request.getMemoryUsage()),
                valueOrUnknown(request.getStorageUsage()),
                valueOrUnknown(request.getRecommendation()),
                valueOrUnknown(request.getSeverity()),
                valueOrUnknown(request.getEstimatedCostSaving()),
                valueOrUnknown(request.getEstimatedMonthlySavingAmount()),
                valueOrUnknown(request.getRationale()),
                userQuestion
        );
    }

    private String extractText(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        StringBuilder text = new StringBuilder();

        if (parts.isArray()) {
            for (JsonNode part : parts) {
                String partText = part.path("text").asText("");
                if (!partText.isBlank()) {
                    text.append(partText);
                }
            }
        }

        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned an empty response");
        }

        return text.toString().trim();
    }

    private String valueOrUnknown(Object value) {
        if (value == null) {
            return "unknown";
        }

        String text = value.toString();
        return text.isBlank() ? "unknown" : text;
    }
}
