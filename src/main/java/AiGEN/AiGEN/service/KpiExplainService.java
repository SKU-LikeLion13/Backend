package AiGEN.AiGEN.service;

import AiGEN.AiGEN.DTO.ExplainResponse;
import AiGEN.AiGEN.domain.AiExplainLog;
import AiGEN.AiGEN.repository.AiExplainLogRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
public class KpiExplainService {
    private final GeminiClient geminiClient;
    private final AiExplainLogRepo logRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ExplainResponse explain(String prompt, String anonId, Long batchId) {
        AiExplainLog log = new AiExplainLog(anonId, batchId, prompt, null);
        log.setStatus("REQUESTED");
        logRepo.save(log);

        try {
            // 1) AI 호출
            String output = geminiClient.generateText(prompt);

            String cleaned = cleanJson(output);

            JsonNode root = objectMapper.readTree(cleaned); // 이제 안전하게 파싱됨
            // 2) 로그 저장
            logRepo.updateStatusAndOutput(log.getId(), "SUCCESS", cleaned);

            // 3) JSON 파싱 시도
            try {


                String headline = root.path("headline").asText("AI KPI 분석 결과");
                List<String> bullets = objectMapper.convertValue(
                        root.path("bullets"), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                List<String> risks = objectMapper.convertValue(
                        root.path("risks"), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                List<String> nextActions = objectMapper.convertValue(
                        root.path("nextActions"), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );

                return new ExplainResponse(
                        headline,
                        bullets != null ? bullets : Collections.emptyList(),
                        risks != null ? risks : Collections.emptyList(),
                        nextActions != null ? nextActions : Collections.emptyList(),
                        output
                );
            } catch (Exception parseEx) {
                // JSON 파싱 실패 시 fallback
                return new ExplainResponse(
                        "AI KPI 분석 결과",
                        List.of(output),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        output
                );
            }

        } catch (Exception e) {
            // 4) 실패 업데이트 + 래핑
            logRepo.updateStatusAndOutput(log.getId(), "FAILED", e.getMessage());
            throw new IllegalStateException("외부 AI 호출 실패: " + e.getMessage(), e);
        }
    }
    private String cleanJson(String output) {
        if (output == null) return "{}";
        // ```json 이랑 ``` 제거
        return output.replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();
    }

}
