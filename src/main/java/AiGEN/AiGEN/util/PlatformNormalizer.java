package AiGEN.AiGEN.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

public class PlatformNormalizer {
    private static final Map<String, String> EXACT = Map.ofEntries(
            Map.entry("NAVER", "NAVER"),
            Map.entry("네이버", "NAVER"),
            Map.entry("GOOGLE", "GOOGLE"),
            Map.entry("구글", "GOOGLE"),
            Map.entry("META", "META"),
            Map.entry("메타", "META"),
            Map.entry("FACEBOOK", "META"),
            Map.entry("INSTAGRAM", "META"),
            Map.entry("FB", "META"),
            Map.entry("IG", "META"),
            Map.entry("TIKTOK", "TIKTOK"),
            Map.entry("틱톡", "TIKTOK"),
            Map.entry("KAKAO", "KAKAO"),
            Map.entry("카카오", "KAKAO"),
            Map.entry("YOUTUBE", "YOUTUBE"),
            Map.entry("유튜브", "YOUTUBE")
    );

    public static String normalize(String raw) {
        if (raw == null) return null;

        // 1) 트림 + NFC + 대문자 + 공백/._- 제거
        String n = Normalizer.normalize(raw.trim(), Normalizer.Form.NFC)
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s._-]+", "");

        // 2) 정확 일치 우선
        String exact = EXACT.get(n);
        if (exact != null) return exact;

        // 3) 부분 포함(루스 매칭) – 필요 시 확장
        if (n.contains("네이버") || n.contains("NAVER")) return "NAVER";
        if (n.contains("구글") || n.contains("GOOGLE") || n.equals("GADS")) return "GOOGLE";
        if (n.contains("메타") || n.contains("FACEBOOK") || n.contains("INSTAGRAM") || n.equals("FB") || n.equals("IG")) return "META";
        if (n.contains("틱톡") || n.contains("TIKTOK")) return "TIKTOK";
        if (n.contains("카카오") || n.contains("KAKAO")) return "KAKAO";
        if (n.contains("유튜브") || n.contains("YOUTUBE") || n.equals("YT")) return "YOUTUBE";

        // 4) 모르면 그대로 반환(새 마스터로 자동 추가)
        return n;
    }
}

