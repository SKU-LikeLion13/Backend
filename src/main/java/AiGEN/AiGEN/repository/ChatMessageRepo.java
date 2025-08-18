package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.ChatMessage;

import java.util.List;

public interface ChatMessageRepo {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> findByAnonIdAndBatchId(String anonId, Long batchId);
    List<ChatMessage> findByAnonId(String anonId);
}
