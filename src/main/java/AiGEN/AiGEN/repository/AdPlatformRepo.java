package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.AdPlatform;
import java.util.List;
import java.util.Optional;

public interface AdPlatformRepo{
//    AdPlatform save(AdPlatform entity);
//    Optional<AdPlatform> findByCode(String code);
//    AdPlatform getRefOrThrow(String code);
    AdPlatform ensureAndGetRef(String rawCode); // 플랫폼 표준화 + 자동추가

//    List<AdPlatform> findAllActive();
//    List<AdPlatform> searchByName(String keyword, int limit, int offset);
//    int setActive(String code, boolean active);

}
