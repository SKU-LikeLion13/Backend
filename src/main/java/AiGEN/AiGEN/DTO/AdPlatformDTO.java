//package AiGEN.AiGEN.DTO;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//
//public class AdPlatformDTO {
//    /** 플랫폼 생성/수정 요청 바디 */
//    @Data
//    public static class AdPlatformReq {
//        private String code;   // 예: NAVER, META, GOOGLE
//        private String name;   // 예: 네이버, 메타, 구글
//        private boolean active;
//    }
//
//    /** 플랫폼 응답 DTO (불변 스타일) */
//    @Data
//    @AllArgsConstructor
//    public static class AdPlatformRes {
//        private final String code;    // PK
//        private final String name;
//        private final boolean active;
//    }
//}
