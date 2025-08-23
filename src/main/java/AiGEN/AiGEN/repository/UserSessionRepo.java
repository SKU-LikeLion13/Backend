package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSessionRepo extends JpaRepository<UserSession, String> {


    // anonId로 조회
    Optional<UserSession> findByAnonId(String anonId);

    // lastSeen만 갱신
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserSession us set us.lastSeen = :now where us.anonId = :anonId")
    int touch(@Param("anonId") String anonId, @Param("now") LocalDateTime now);

}

