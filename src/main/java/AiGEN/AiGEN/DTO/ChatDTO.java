package AiGEN.AiGEN.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ChatDTO {

    /**
     * 사용자의 채팅 메시지를 받기 위한 DTO입니다.
     */
    @Data
    @NoArgsConstructor
    public static class ChatReq {
        private String message;
        private Long batchId; // 특정 데이터와 관련된 대화인 경우 사용
    }

    /**
     * 챗봇의 응답을 사용자에게 반환하기 위한 DTO입니다.
     */
    @Data
    @AllArgsConstructor
    public static class ChatRes {
        private String response;
        private String anonId;
        private Long batchId;
    }
}
