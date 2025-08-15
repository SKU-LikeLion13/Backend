package AiGEN.AiGEN.controller;

import AiGEN.AiGEN.DTO.UploadBatchDTO;
import AiGEN.AiGEN.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "Uploads", description = "파일 업로드/파싱 및 최근 파일명 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadService uploadService;

//    @Operation(
//            summary = "업로드 시작",
//            description = "X-Anon-Id 헤더 기반으로 배치를 생성하고 파일명을 기록합니다.",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "배치 생성됨",
//                            content = @Content(schema = @Schema(implementation = UploadBatchDTO.UploadBatchRes.class)))
//            }
//    )
//    @PostMapping(value = "/start", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public UploadBatchDTO.UploadBatchRes start(
//            @RequestHeader("X-Anon-Id") String anonId,
//            @RequestBody UploadBatchDTO.StartUploadReq req
//    ) {
//        UploadBatch b = uploadService.startUpload(anonId, req.getFilename());
//        return new UploadBatchDTO.UploadBatchRes(
//                b.getId(),
//                b.getUserSession().getAnonId(),
//                b.getFilename(),
//                b.getUploadedAt()
//        );
//    }

    @Operation(
            summary = "업로드 파일명 목록",
            description = "사용자(anonId)의 최근 업로드 파일명 리스트를 반환합니다. limit로 개수 제한합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "파일명 리스트",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping(value = "/filenames", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> listFilenames(
            @RequestParam String anonId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return uploadService.listFilenames(anonId, limit);
    }

    @Operation(
            summary = "파일 업로드 및 파싱 (한 번에)",
            description = """
            프론트가 파일만 전송하면 서버가 파일명을 기록하여 배치를 생성하고,
            즉시 파싱하여 AdData로 저장한 뒤 결과를 반환합니다.

            - 헤더: X-Anon-Id 필수
            - 본문: multipart/form-data, key: file
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "파싱 결과",
                            content = @Content(schema = @Schema(implementation = UploadBatchDTO.ParseResultRes.class)))
            }
    )
    @PostMapping(
            value = "/parse",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UploadBatchDTO.ParseResultRes parseDirect(
            @RequestHeader("X-Anon-Id") String anonId,
            @RequestPart("file") MultipartFile file
    ) {
        return uploadService.parseDirect(anonId, file);
    }

//    @Operation(
//            summary = "파일 파싱/저장",
//            description = "업로드한 엑셀/CSV를 파싱하여 AdData로 저장하고, 요약 정보를 반환합니다.",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "파싱 결과",
//                            content = @Content(schema = @Schema(implementation = UploadBatchDTO.ParseResultRes.class)))
//            }
//    )
//    @PostMapping(
//            value = "/{batchId}/parse",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public UploadBatchDTO.ParseResultRes parse(
//            @RequestHeader("X-Anon-Id") String anonId,
//            @PathVariable Long batchId,
//            @RequestPart("file") MultipartFile file
//    ) {
//        // 서비스가 DTO를 바로 리턴
//        return uploadService.parseAndSave(batchId, anonId, file);
//    }
}
