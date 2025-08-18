package AiGEN.AiGEN.service;

import AiGEN.AiGEN.DTO.AdDataDTO;
import AiGEN.AiGEN.domain.ChatMessage;
import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.repository.ChatMessageRepo;
import AiGEN.AiGEN.repository.UploadBatchRepo;
import AiGEN.AiGEN.repository.UserSessionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatbotService {
    private final GeminiClient geminiClient;
    private final ChatMessageRepo chatMessageRepo;
    private final UploadBatchRepo uploadBatchRepo;
    private final AdDataService adDataService;
    private final UserSessionRepo userSessionRepo;

    /**
     * 사용자 입력에 대해 챗봇의 응답을 생성하고 대화 기록을 저장합니다.
     *
     * @param anonId 사용자 세션 ID
     * @param userMessage 사용자의 메시지
     * @param batchId 특정 데이터와 관련된 대화일 경우 배치 ID (선택 사항, null 가능)
     * @return 챗봇의 응답 텍스트
     */
    public String getChatResponse(String anonId, String userMessage, Long batchId) {
        // 1. batchId가 null인 경우, 가장 최근 배치 ID를 가져옴
        if (batchId == null) {
            batchId = uploadBatchRepo.findLatestIdBySession(anonId).orElse(null);
        }

        // 2. 사용자 메시지를 데이터베이스에 저장
        ChatMessage userMsg = new ChatMessage(anonId, batchId, "user", userMessage);
        chatMessageRepo.save(userMsg);

        // 3. 대화 기록 및 광고 데이터 KPI 가져오기
        List<ChatMessage> history = chatMessageRepo.findByAnonIdAndBatchId(anonId, batchId);
        List<AdDataDTO.PlatformTotalsRes> adDataTotals = null;
        if (batchId != null) {
            // 1. anonId를 이용해 DB에서 실제 UserSession 객체를 조회
            UserSession userSession = userSessionRepo.findByAnonId(anonId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 세션입니다."));

            // 2. 조회한 실제 UserSession 객체를 서비스에 전달
            adDataTotals = adDataService.reportByPlatformAll(userSession);
        }

        // 4. 프롬프트 구성
        String prompt = buildPrompt(userMessage, history, adDataTotals);

        // 5. 제미나이 API 호출
        String geminiResponse = geminiClient.generateText(prompt);

        // 6. 챗봇 응답을 데이터베이스에 저장
        ChatMessage assistantMsg = new ChatMessage(anonId, batchId, "assistant", geminiResponse);
        chatMessageRepo.save(assistantMsg);

        return geminiResponse;
    }

    /**
     * 대화 기록과 광고 데이터를 기반으로 AI에 전달할 프롬프트를 구성합니다.
     * @param userMessage 사용자의 현재 메시지
     * @param history 대화 기록 리스트
     * @param adDataTotals 플랫폼별 KPI 요약 데이터
     * @return AI 호출을 위한 최종 프롬프트 문자열
     */
    private String buildPrompt(String userMessage, List<ChatMessage> history, List<AdDataDTO.PlatformTotalsRes> adDataTotals) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 광고 데이터 분석 전문가입니다. 사용자의 질문에 답변하기 위해 제공된 광고 데이터를 활용하세요.\n\n");

        // 데이터가 있는 경우에만 요약하여 프롬프트에 추가
        if (adDataTotals != null && !adDataTotals.isEmpty()) {
            sb.append("--- 광고 데이터 요약 (플랫폼별 KPI) ---\n");

            adDataTotals.forEach(t -> {
                sb.append(String.format(
                        "플랫폼=%s | 비용=%d원 | 클릭수=%d | 전환수=%d | 매출=%d원 | CPC=%.2f원 | CVR=%.2f%% | ROAS=%.2f%% | ROI=%.2f%%%n",
                        t.getPlatformCode(), t.getCostSum(), t.getClicksSum(), t.getConvSum(), t.getRevenueSum(),
                        t.getCpc().doubleValue(), t.getCvr().doubleValue(), t.getRoas().doubleValue(), t.getRoi().doubleValue()
                ));
            });
            sb.append("\n");
        } else {
            sb.append("--- 광고 데이터 없음 ---\n\n");
        }

        // 대화 기록 추가
        sb.append("--- 대화 기록 ---\n");
        // 대화 기록을 역순으로 순회하여 프롬프트에 추가 (가장 최근 대화가 아래로 오게)
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatMessage msg = history.get(i);
            sb.append(String.format("%s: %s\n", msg.getRole(), msg.getContent()));
        }

        sb.append(String.format("\nuser: %s\n", userMessage));
        sb.append("assistant: ");

        return sb.toString();
    }
}

