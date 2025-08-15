//package AiGEN.AiGEN.service;
//
//import AiGEN.AiGEN.domain.AdPlatform;
//import AiGEN.AiGEN.repository.AdPlatformRepo;
//import AiGEN.AiGEN.util.PlatformNormalizer;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class AdPlatformService {
//    private final AdPlatformRepo adPlatformRepo;
//
//    /** 방법 A: 원문 코드 표준화 → 없으면 자동 추가 → getReference 반환 */
//    @Transactional
//    public AdPlatform ensureAndGetRef(String rawCode) {
//        return adPlatformRepo.ensureAndGetRef(rawCode);
//    }
//
//    /** 존재 보장 레퍼런스 */
//    @Transactional(readOnly = true)
//    public AdPlatform getRefOrThrow(String code) {
//        return adPlatformRepo.getRefOrThrow(code);
//    }
//
//    /** 활성 목록 */
//    @Transactional(readOnly = true)
//    public List<AdPlatform> findAllActive() {
//        return adPlatformRepo.findAllActive();
//    }
//
//    /** 이름 검색(부분 일치) */
//    @Transactional(readOnly = true)
//    public List<AdPlatform> searchByName(String keyword, int limit, int offset) {
//        return adPlatformRepo.searchByName(keyword, limit, offset);
//    }
//
//    /** 활성/비활성 토글 */
//    @Transactional
//    public void setActive(String code, boolean active) {
//        adPlatformRepo.setActive(code, active);
//    }
//
//    /** 이름/활성 상태 저장(신규 or 갱신) */
//    @Transactional
//    public AdPlatform save(String code, String name, boolean active) {
//        return adPlatformRepo.save(new AdPlatform(code, name, active));
//    }
//}
