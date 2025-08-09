package AiGEN.AiGEN.service;

import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.repository.UserSessionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSessionService {
    private final UserSessionRepo repo;

    /**
     * anonId로 세션 조회. 없으면 신규 생성하여 반환.
     */
    @Transactional
    public UserSession getOrCreate(String anonId) {
        Optional<UserSession> found = repo.findById(anonId);
        if (found.isPresent()) return found.get();
        // 생성자만으로 anonId 설정 → createdAt/lastSeen은 Auditing이 채움
        return repo.save(new UserSession(anonId));
    }

    /**
     * 접속 갱신. 존재하면 lastSeen만 업데이트, 없으면 생성.
     * 반환값: 업데이트/생성된 세션
     */
    @Transactional
    public UserSession touch(String anonId) {
        int updated = repo.touch(anonId, LocalDateTime.now());
        if (updated == 0) {
            // 최초 접속이면 생성
            return repo.save(new UserSession(anonId));
        }
        // 이미 존재 → 최신 엔티티 반환(선택)
        return repo.findById(anonId).orElseGet(() -> new UserSession(anonId));
    }

    /**
     * 단순 조회용
     */
    @Transactional(readOnly = true)
    public Optional<UserSession> find(String anonId) {
        return repo.findById(anonId);
    }
}
