// src/main/java/AiGEN/AiGEN/service/DownloadService.java
package AiGEN.AiGEN.service;

import AiGEN.AiGEN.config.R2Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ReelsDownloadService {

    private final S3Client r2;
    private final R2Properties props;

    public ResponseEntity<?> download(String key, String filename) {
        try {
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> in = r2.getObject(req);

            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Download failed: " + e.getMessage());
        }
    }
}
