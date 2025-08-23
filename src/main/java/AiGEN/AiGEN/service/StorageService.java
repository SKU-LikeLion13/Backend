//package AiGEN.AiGEN.service;
//
//import AiGEN.AiGEN.config.R2Properties;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//
//import java.time.LocalDate;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class StorageService {
//    private final S3Client r2;
//    private final R2Properties props;
//
//    public record UploadResult(String key, String url) {}
//
//    public UploadResult upload(MultipartFile file, String anonId) throws Exception {
//        String ext = extractExt(file.getOriginalFilename());
//        String key = buildKey(ext, anonId);
//
//        PutObjectRequest req = PutObjectRequest.builder()
//                .bucket(props.getBucket())
//                .key(key)
//                .contentType(safeContentType(file.getContentType()))
//                .build();
//
//        r2.putObject(req, RequestBody.fromBytes(file.getBytes()));
//
//        String url = buildPublicUrl(key);
//        return new UploadResult(key, url);
//    }
//
//    private String buildKey(String ext, String anonId) {
//        LocalDate d = LocalDate.now();
//        String safeAnon = (anonId == null || anonId.isBlank())
//                ? "anon" : anonId.replaceAll("[^a-zA-Z0-9_-]", "_");
//        return "uploads/%d/%02d/%02d/%s/%s.%s".formatted(
//                d.getYear(), d.getMonthValue(), d.getDayOfMonth(),
//                safeAnon, UUID.randomUUID(), ext
//        );
//    }
//
//    private String buildPublicUrl(String key) {
//        if (props.getPublicBase() != null && !props.getPublicBase().isBlank()) {
//            return props.getPublicBase().replaceAll("/$", "") + "/" + key;
//        }
//        return props.getEndpoint().replaceAll("/$", "")
//                + "/" + props.getBucket() + "/" + key;
//    }
//
//    private static String extractExt(String name) {
//        if (name == null) return "bin";
//        int i = name.lastIndexOf('.');
//        return (i > -1 && i < name.length() - 1)
//                ? name.substring(i + 1).toLowerCase()
//                : "bin";
//    }
//
//    private static String safeContentType(String ct) {
//        return (ct == null || ct.isBlank()) ? "application/octet-stream" : ct;
//    }
//}
