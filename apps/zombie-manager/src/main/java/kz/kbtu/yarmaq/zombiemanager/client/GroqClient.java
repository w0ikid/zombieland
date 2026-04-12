package kz.kbtu.yarmaq.zombiemanager.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kbtu.yarmaq.zombiemanager.dto.SortieOutcome;
import kz.kbtu.yarmaq.zombiemanager.dto.GroqRequest;
import kz.kbtu.yarmaq.zombiemanager.dto.GroqResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroqClient {

    private final WebClient groqWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
        Ты — генератор результатов действий в survival игре (зомби апокалипсис).
        Правила:
        - Отвечай строго JSON
        - Без лишнего текста
        - Не добавляй поля
        Формат:
        {
          "description": "string",
          "outcome": "success | partial | fail",
          "resources": {
            "FOOD": number,
            "AMMO": number,
            "MATERIALS": number
          }
        }
        Ограничения:
        - FOOD: -10..40
        - AMMO: -30..0
        - MATERIALS: -5..25
        """;

    public SortieOutcome generateOutcome(String userAction) {
        GroqRequest request = GroqRequest.builder()
                .model("llama-3.3-70b-versatile")
                .messages(List.of(
                        new GroqRequest.Message("system", SYSTEM_PROMPT),
                        new GroqRequest.Message("user", userAction)
                ))
                .temperature(0.7)
                .build();

        log.info("Requesting Groq outcome for action: {}", userAction);

        GroqResponse response = groqWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroqResponse.class)
                .block();

        if (response == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("Empty response from Groq API");
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        log.debug("Groq raw response: {}", content);

        try {
            // Remove markdown code blocks if the AI accidentally included them
            String cleanedJson = content.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7, cleanedJson.length() - 3).trim();
            } else if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(3, cleanedJson.length() - 3).trim();
            }
            
            return objectMapper.readValue(cleanedJson, SortieOutcome.class);
        } catch (Exception e) {
            log.error("Failed to parse Groq response: {}", content, e);
            throw new RuntimeException("Failed to process game outcome", e);
        }
    }
}
