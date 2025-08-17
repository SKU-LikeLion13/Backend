//package AiGEN.AiGEN.service;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import java.time.Duration;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class OpenRouterClient {
//    private final WebClient webClient;
//    private final String model;
//
//    public OpenRouterClient(
//            @Value("${ai.openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl,
//            @Value("${ai.openrouter.api-key:}") String apiKey,
//            @Value("${ai.openrouter.model:meta-llama/llama-3.1-8b-instruct}") String model
//    ) {
//        if (apiKey == null || apiKey.isBlank()) {
//            throw new IllegalStateException("OPENROUTER_API_KEY가 설정되어 있지 않습니다.");
//        }
//        this.model = model;
//        this.webClient = WebClient.builder()
//                .baseUrl(baseUrl)
//                .defaultHeader("Authorization", "Bearer " + apiKey)
//                .defaultHeader("X-Title", "AiGEN KPI Explain")
//                .build();
//    }
//
//    public String chat(List<Message> messages) {
//        Map<String, Object> payload = Map.of("model", model, "messages", messages);
//
//        Map<?, ?> res = webClient.post()
//                .uri("/chat/completions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(payload)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .timeout(Duration.ofSeconds(20))
//                .block();
//
//        if (res == null) return "";
//        List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");
//        if (choices == null || choices.isEmpty()) return "";
//        Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
//        return msg == null ? "" : (String) msg.getOrDefault("content", "");
//    }
//
//    @Data
//    @AllArgsConstructor
//    public static class Message {
//        private String role;    // system | user | assistant
//        private String content;
//    }
//}
