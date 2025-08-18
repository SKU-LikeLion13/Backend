package AiGEN.AiGEN.controller;

import AiGEN.AiGEN.DTO.ChatDTO;
import AiGEN.AiGEN.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "챗봇 API", description = "AI 챗봇과 대화하는 기능을 제공합니다.")
public class ChatController {
    private final ChatbotService chatbotService;

    @Operation(
            summary = "챗봇과 대화하기",
            description = "사용자의 메시지를 받아 AI 챗봇의 응답을 반환합니다. 대화 기록이 데이터베이스에 저장됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "챗봇 응답 성공",
                            content = @Content(schema = @Schema(implementation = ChatDTO.ChatRes.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (예: 필수 헤더 누락)",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류 (예: AI 호출 실패)",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    @PostMapping("/send")
    public ChatDTO.ChatRes sendMessage(
            @Parameter(description = "사용자 익명 ID", required = true, example = "X-Anon-Id: 1a2b3c4d")
            @RequestHeader(name = "X-Anon-Id") String anonId,
            @RequestBody ChatDTO.ChatReq chatRequest) {

        if (anonId == null || anonId.isBlank()) {
            throw new IllegalArgumentException("익명 사용자 ID가 누락되었습니다.");
        }

        String chatbotResponse = chatbotService.getChatResponse(
                anonId,
                chatRequest.getMessage(),
                chatRequest.getBatchId()
        );

        ChatDTO.ChatRes response = new ChatDTO.ChatRes(chatbotResponse, anonId, chatRequest.getBatchId());
        return new ChatDTO.ChatRes(chatbotResponse, anonId, chatRequest.getBatchId());
    }
}
