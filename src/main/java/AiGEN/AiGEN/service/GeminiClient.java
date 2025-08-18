package AiGEN.AiGEN.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class GeminiClient {
    private final WebClient webClient;

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.0-flash}")
    private String model;

    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY가 설정되어 있지 않습니다.");
        }

        String path = String.format("/v1beta/models/%s:generateContent", model);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            Map<String, Object> res = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(baseUrl.replaceFirst("^https?://", ""))
                            .path(path)
                            .queryParam("key", apiKey) // 쿼리스트링 방식
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        // 오류 메시지 깔끔히 노출
                        String msg = String.format("%d %s from POST %s%s",
                                e.getRawStatusCode(), e.getStatusText(), baseUrl, path);
                        return Mono.error(new IllegalStateException("Gemini 호출 실패: " + msg, e));
                    })
                    .block();

            // 응답 파싱 (candidates[0].content.parts[*].text 합치기)
            if (res == null) return "";
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) res.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "";

            Map<String, Object> first = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) first.get("content");
            if (content == null) return "";

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> p : parts) {
                Object t = p.get("text");
                if (t != null) sb.append(t.toString());
            }
            return sb.toString().trim();

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Gemini 호출 중 알 수 없는 오류", ex);
        }
    }
}