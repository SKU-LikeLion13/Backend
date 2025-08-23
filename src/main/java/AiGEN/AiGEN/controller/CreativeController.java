package AiGEN.AiGEN.controller;

import AiGEN.AiGEN.service.CreativeService;
import AiGEN.AiGEN.service.ReelsDownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/creative")
@Tag(name = "Creative Request", description = "릴스/영상 생성 API")
public class CreativeController {
    private final CreativeService creativeService;
    private final ReelsDownloadService reelsDownloadService;

    @Operation(
            summary = "이미지로 릴스 생성 후 즉시 다운로드로 리다이렉트",
            description = """
                    업로드한 이미지들로 1080x1920 릴스(mp4)를 생성합니다.
                    생성이 끝나면 303 See Other로 `/api/creative/reels/download`에 리다이렉트되어 브라우저에서 바로 다운로드가 시작됩니다.
                    """)
    @ApiResponse(responseCode = "303", description = "생성 완료 → 다운로드로 리다이렉트")
    @ApiResponse(responseCode = "400", description = "요청 값 오류")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")

    @PostMapping(value = "/reels", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> makeReels(
            @Parameter(description = "익명 세션 ID", required = true)
            @RequestHeader("X-Anon-Id") String anonId,

            @Parameter(description = "이미지 파일들(image/*). 같은 키(files)로 여러 개 업로드", required = true)
            @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "브랜드명", required = true, example = "테스트")
            @RequestParam("brandName") String brandName,

            @Parameter(description = "각 이미지 구간에 표시될 텍스트(줄바꿈으로 분리)", example = "첫 줄\n둘째 줄")
            @RequestParam(value = "prompt", required = false) String prompt
    ) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("files is required");
            }
            for (MultipartFile f : files) {
                String ct = f.getContentType();
                if (ct == null || !ct.startsWith("image/")) {
                    return ResponseEntity.badRequest().body("Only image/* allowed");
                }
            }

            var rr = creativeService.createReels(anonId, files, brandName, prompt);

            // key와 filename 인코딩
            String keyEnc = URLEncoder.encode(rr.key(), StandardCharsets.UTF_8).replace("+", "%20");
            String fileNameEnc = URLEncoder.encode("reels.mp4", StandardCharsets.UTF_8).replace("+", "%20");

            // 절대 URL Location 생성 (현재 호스트 기준)
            URI downloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/creative/reels/download")
                    .queryParam("key", keyEnc)
                    .queryParam("filename", fileNameEnc)
                    .build(true)    // 인코딩 유지
                    .toUri();

            return ResponseEntity.status(303) // See Other → GET으로 따라감
                    .location(downloadUri)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Create reels failed: " + e.getMessage());
        }
    }

    @GetMapping("/reels/download")
    public ResponseEntity<?> download(
            @RequestParam("key") String key,
            @RequestParam(value = "filename", defaultValue = "reels.mp4") String filename
    ) {
        return reelsDownloadService.download(key, filename);
    }

}
