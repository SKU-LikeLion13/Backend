//package AiGEN.AiGEN.service;
//
//import com.google.cloud.storage.BlobInfo;
//import com.google.cloud.storage.Storage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class GcsService {
//    private final Storage storage; // Google Cloud Storage 클라이언트 자동 주입
//
//    @Value("${google.cloud.gcp.storage.bucket-name}")
//    private String bucketName;
//
//    /**
//     * MultipartFile을 GCS에 업로드하고 공개 URL을 반환합니다.
//     */
//    public String uploadImage(MultipartFile file) throws IOException {
//        // 1. 고유한 파일 이름 생성 (중복 방지)
//        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//
//        // 2. GCS에 업로드할 파일 정보 생성
//        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
//                .setContentType(file.getContentType())
//                .build();
//
//        // 3. GCS에 파일 업로드
//        storage.create(blobInfo, file.getBytes());
//
//        // 4. 업로드된 파일의 공개 URL 반환
//        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
//    }
//}
