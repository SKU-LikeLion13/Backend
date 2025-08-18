package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.ChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepoImpl implements ChatMessageRepo {
    private final EntityManager em;

    /**
     * 새로운 채팅 메시지를 저장합니다.
     * @param message 저장할 ChatMessage 객체
     * @return 저장된 ChatMessage 객체
     */
    @Override
    public ChatMessage save(ChatMessage message) {
        try {
            if (message.getId() == null) {
                em.persist(message);
                return message;
            } else {
                return em.merge(message);
            }
        } catch (PersistenceException e) {
            // 데이터 무결성 제약 위반 등 JPA 관련 영속성 예외 처리
            throw new IllegalArgumentException("메시지 저장 중 데이터 무결성 제약을 위반했습니다.", e);
        } catch (Exception e) {
            // 그 외 알 수 없는 오류 처리
            throw new IllegalStateException("메시지 저장 중 알 수 없는 오류 발생", e);
        }
    }

    /**
     * 특정 사용자의 특정 배치(batchId)와 관련된 대화 기록을 최신순으로 가져옵니다.
     * @param anonId 사용자 ID
     * @param batchId 배치 ID
     * @return 대화 기록 리스트
     */
    @Override
    public List<ChatMessage> findByAnonIdAndBatchId(String anonId, Long batchId) {
        try {
            return em.createQuery("""
                    select c
                    from ChatMessage c
                    where c.anonId = :anonId and c.batchId = :batchId
                    order by c.createdAt desc
                    """, ChatMessage.class)
                    .setParameter("anonId", anonId)
                    .setParameter("batchId", batchId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("채팅 기록 조회 중 오류 발생", e);
        }
    }

    /**
     * 특정 사용자의 모든 대화 기록을 최신순으로 가져옵니다.
     * @param anonId 사용자 ID
     * @return 대화 기록 리스트
     */
    @Override
    public List<ChatMessage> findByAnonId(String anonId) {
        try {
            return em.createQuery("""
                    select c
                    from ChatMessage c
                    where c.anonId = :anonId
                    order by c.createdAt desc
                    """, ChatMessage.class)
                    .setParameter("anonId", anonId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("채팅 기록 조회 중 오류 발생", e);
        }
    }
}
