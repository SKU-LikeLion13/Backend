//package AiGEN.AiGEN.service;
//
//import AiGEN.AiGEN.DTO.CreativeRequestDTO;
//import AiGEN.AiGEN.domain.CreativeRequest;
//import AiGEN.AiGEN.domain.UserSession;
//import AiGEN.AiGEN.repository.CreativeRequestRepo;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//
//
//@Service
//@RequiredArgsConstructor
//public class CreativeRequestService {
//    private final CreativeRequestRepo creativeRequestRepo;
//    private final UserSessionService userSessionService;
//    private final VeoApiClient veoApiClient;
//    private final GcsService gcsService;
//
//    @Transactional
//    public String createVideo(String anonId, CreativeRequestDTO dto, MultipartFile imageFile) throws Exception {
//        // 1. GCS에 이미지 업로드하고 URL 받아오기
//        String imageUrl = gcsService.uploadImage(imageFile);
//
//        // 2. 세션 확보
//        UserSession session = userSessionService.getOrCreate(anonId);
//
//        // 3. 엔티티 변환 및 저장
//        CreativeRequest request = new CreativeRequest(
//                session,
//                dto.getBrandName(),
//                dto.getHashtags(),
//                dto.getPrompt()
//        );
//        creativeRequestRepo.save(request);
//
//        // 4. 프롬프트 빌드
//        String finalPrompt = buildPrompt(request);
//
//        // 5. Veo API 호출 (GCS에서 받은 URL 사용)
//        return veoApiClient.generateVideo(finalPrompt, imageUrl);
//    }
//
//    private String buildPrompt(CreativeRequest request) {
//        // ... (기존 buildPrompt 메소드는 그대로 유지)
//        String basePrompt =
//                "브랜드명: " + request.getBrandName() + "\n" +
//                        "광고 문구: " + request.getPrompt() + "\n";
//
//        String hashtag = request.getHashtags();
//        String stylePrompt = "";
//
//        if (hashtag != null) {
//            if (hashtag.contains("#재밌는")) {
//                stylePrompt += "채도 20% ↑, 명도 10% ↑, 대비 15% ↑\n";
//            }
//            if (hashtag.contains("#편안")) {
//                stylePrompt += "채도 15% ↓, 명도 5% ↓, 대비 10% ↓, 블루톤 +5%\n";
//            }
//            if (hashtag.contains("#핫플")) {
//                stylePrompt += "채도 25% ↑, 명도 5% ↑, 대비 20% ↑, 레드·오렌지 톤 +15%\n";
//            }
//            if (hashtag.contains("#먹방")) {
//                stylePrompt += "채도 30% ↑, 명도 5% ↑, 대비 10% ↑, 따뜻한 톤 (옐로우·레드 계열 +10%)\n";
//            }
//        }
//
//        return basePrompt + "스타일: " + stylePrompt;
//    }
//}
