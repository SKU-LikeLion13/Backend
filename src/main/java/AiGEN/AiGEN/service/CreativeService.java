package AiGEN.AiGEN.service;

import AiGEN.AiGEN.config.R2Properties;
import AiGEN.AiGEN.domain.CreativeRequest;
import AiGEN.AiGEN.domain.UserSession;
import AiGEN.AiGEN.repository.CreativeRequestRepo;
import AiGEN.AiGEN.repository.UserSessionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreativeService {
    private final S3Client r2;
    private final R2Properties props;
    private final CreativeRequestRepo creativeReqRepo;
    private final UserSessionService userSessionService;


    @Value("${app.video.ffmpegPath:ffmpeg}")
    private String ffmpegPath;

    @Value("${app.video.width:1080}")
    private int width;

    @Value("${app.video.height:1920}")
    private int height;

    @Value("${app.video.fontPath:C:\\\\Windows\\\\Fonts\\\\malgun.ttf}")
    private String fontPath;

    @Value("${app.video.secondsPerImage:2}")
    private int secondsPerImage;

    public record ReelsResult(String key, String url, long durationSec) {}

    @Transactional
    public ReelsResult createReels(
            String anonId,
            List<MultipartFile> images,
            String brandName,
            String prompt
    ) throws Exception {

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("No images supplied");
        }

        UserSession session = userSessionService.touch(anonId);

        // 1) 작업 폴더
        File workDir = Files.createTempDirectory("reels_").toFile();
        workDir.deleteOnExit();

        // 2) 이미지 저장 (이름 0000.jpg 형식)
        List<File> localImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile mf = images.get(i);
            String ext = extractExt(mf.getOriginalFilename());
            File localFile = new File(workDir, String.format("%04d.%s", i, ext));
            mf.transferTo(localFile);
            localImages.add(localFile);
        }

        // 3) concat용 filelist.txt (경로는 슬래시, 따옴표로 감싸기)
        File fileList = new File(workDir, "filelist.txt");
        try (BufferedWriter bw = Files.newBufferedWriter(fileList.toPath(), StandardCharsets.UTF_8)) {
            for (File img : localImages) {
                String path = img.getAbsolutePath().replace("\\", "/");
                bw.write("file '" + path + "'\n");
                bw.write("duration " + secondsPerImage + "\n");
            }
            // 마지막 프레임 반복(FFmpeg concat 규칙)
            String lastPath = localImages.get(localImages.size() - 1).getAbsolutePath().replace("\\", "/");
            bw.write("file '" + lastPath + "'\n");
        }

        // 4) drawtext용 텍스트: 프롬프트를 줄 단위로 쪼개서 각 이미지 구간에 1줄씩
        List<String> textRelNames = new ArrayList<>(); // workDir 내 상대경로 파일명 목록
        if (prompt != null && !prompt.isBlank()) {
            String[] lines = prompt.replace("\r\n", "\n").split("\n");
            int limit = Math.min(lines.length, localImages.size()); // 이미지 수만큼만 사용
            for (int i = 0; i < limit; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue; // 빈 줄은 스킵 (원하면 " " 로 대체 가능)
                String name = String.format("text_%04d.txt", i);
                Files.writeString(new File(workDir, name).toPath(), line, StandardCharsets.UTF_8);
                textRelNames.add(name);
            }
        }

// 4-1) 폰트: workDir로 복사(상대경로 사용)
        String fontRelName = null;
        if (fontPath != null && !fontPath.isBlank()) {
            File fontSrc = new File(fontPath);
            if (fontSrc.exists()) {
                fontRelName = "font.ttf";
                Files.copy(
                        fontSrc.toPath(),
                        new File(workDir, fontRelName).toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }
        }

// 5) 필터 문자열: "이미지마다 한 줄" 표시
        String filter = buildFilterRelativePerImage(fontRelName, textRelNames, localImages.size());


        // 6) 출력 파일
        File outMp4 = new File(workDir, "out.mp4");

        // 7) ffmpeg 명령 (인자 배열 방식)
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-hide_banner");
        cmd.add("-y");
        cmd.add("-f"); cmd.add("concat");
        cmd.add("-safe"); cmd.add("0");
        cmd.add("-i"); cmd.add(fileList.getAbsolutePath());
        cmd.add("-vf"); cmd.add(filter);
        cmd.add("-r"); cmd.add("30");
        cmd.add("-c:v"); cmd.add("libx264");
        cmd.add("-pix_fmt"); cmd.add("yuv420p");
        cmd.add("-movflags"); cmd.add("+faststart");
        cmd.add(outMp4.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir);          // 상대경로 기준을 workDir로
        pb.redirectErrorStream(true);   // stderr → stdout

        System.out.println("--- Executing FFmpeg Command (tokens) ---");
        System.out.println(String.join(" ", cmd));
        System.out.println("-----------------------------------------");

        Process p = pb.start();

        // 8) ffmpeg 출력 캡처
        StringBuilder ffLog = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ffLog.append(line).append('\n');
                System.out.println("[FFMPEG] " + line);
            }
        }

        int exitCode = p.waitFor();
        if (exitCode != 0 || !outMp4.exists() || outMp4.length() == 0) {
            String preview = ffLog.length() > 3000 ? ffLog.substring(0, 3000) + "..." : ffLog.toString();
            throw new IllegalStateException("ffmpeg failed. exit=" + exitCode + "\n--- ffmpeg log preview ---\n" + preview);
        }

        // 9) R2 업로드 + DB
        String key = buildKey("mp4", anonId);
        r2.putObject(
                PutObjectRequest.builder()
                        .bucket(props.getBucket())
                        .key(key)
                        .contentType("video/mp4")
                        .build(),
                RequestBody.fromFile(outMp4.toPath())
        );
        String url = buildPublicUrl(key);

        CreativeRequest cr = new CreativeRequest(session, brandName, prompt);
        creativeReqRepo.save(cr);

        long duration = (long) images.size() * secondsPerImage;
        return new ReelsResult(key, url, duration);
    }

    /**
     * 9:16 맞춤 + drawtext(textfile) 적용
     * - 절대경로 대신 workDir 내 상대경로(font.ttf, text.txt)만 사용
     * - 필드: fontRelName은 null일 수 있음(그 경우 시스템 기본 폰트 검색)
     */
    private String buildFilterRelative(String fontRelName, String textRelName) {
        // 1) 1080x1920보다 작으면 키우고(비율 유지), 2) 정확히 1080x1920으로 crop, 3) 픽셀 포맷 고정
        String base = String.format(
                "scale=%d:%d:force_original_aspect_ratio=increase," +  // 크게 맞춤(비율 유지)
                        "crop=%d:%d," +                                        // 정확히 잘라내기
                        "format=yuv420p",
                width, height, width, height
        );

        if (textRelName == null) {
            return base; // 텍스트 없으면 여기서 끝
        }

        String fontPart = (fontRelName == null || fontRelName.isBlank()) ? "" : "fontfile=" + fontRelName + ":";

        // 최종 프레임(=1080x1920) 좌표 기준으로 중앙 하단
        String draw = "drawtext=" + fontPart
                + "textfile=" + textRelName + ":"
                + "fontcolor=white:fontsize=64:"
                + "box=1:boxcolor=black@0.5:boxborderw=20:"
                + "shadowx=0:shadowy=0:shadowcolor=black@0.6:"
                + "x=(w-text_w)/2:y=h-text_h-40";

        // base 뒤에 drawtext만 붙이면 끝 (pad 없음!)
        return base + "," + draw;
    }




    private String buildKey(String ext, String anonId) {
        LocalDate d = LocalDate.now();
        String safeAnon = (anonId == null || anonId.isBlank())
                ? "anon" : anonId.replaceAll("[^a-zA-Z0-9_-]", "_");
        return "videos/%d/%02d/%02d/%s/%s.%s".formatted(
                d.getYear(), d.getMonthValue(), d.getDayOfMonth(),
                safeAnon, UUID.randomUUID(), ext
        );
    }

    private String buildPublicUrl(String key) {
        if (props.getPublicBase() != null && !props.getPublicBase().isBlank()) {
            return props.getPublicBase().replaceAll("/$", "") + "/" + key;
        }
        return props.getEndpoint().replaceAll("/$", "")
                + "/" + props.getBucket() + "/" + key;
    }

    private static String extractExt(String name) {
        if (name == null) return "png";
        int i = name.lastIndexOf('.');
        return (i > -1 && i < name.length() - 1)
                ? name.substring(i + 1).toLowerCase()
                : "png";
    }
    /**
     * 이미지 i가 보이는 구간 [i*secondsPerImage, (i+1)*secondsPerImage) 에
     * i번째 줄 텍스트만 표시. (패딩 전 프레임 기준 w/h 좌표)
     */
    private String buildFilterRelativePerImage(String fontRelName, List<String> textRelNames, int imgCount) {
        // 1) 비율 유지 확대한 뒤 1080x1920으로 crop, 포맷 고정
        String base = String.format(
                "scale=%d:%d:force_original_aspect_ratio=increase," +
                        "crop=%d:%d," +
                        "format=yuv420p",
                width, height, width, height
        );

        StringBuilder draws = new StringBuilder();
        if (textRelNames != null && !textRelNames.isEmpty()) {
            int n = Math.min(textRelNames.size(), imgCount);
            for (int i = 0; i < n; i++) {
                double t0 = i * (double) secondsPerImage;
                double t1 = (i + 1) * (double) secondsPerImage - 0.001;
                String fontPart = (fontRelName == null || fontRelName.isBlank()) ? "" : "fontfile=" + fontRelName + ":";

                if (draws.length() > 0) draws.append(",");

                draws.append("drawtext=")
                        .append(fontPart)
                        .append("textfile=").append(textRelNames.get(i)).append(":")
                        .append("fontcolor=white:fontsize=64:")
                        .append("box=1:boxcolor=black@0.5:boxborderw=20:")
                        .append("shadowx=0:shadowy=0:shadowcolor=black@0.6:")
                        // 최종 프레임(1080x1920) 기준 위치
                        .append("x=(w-text_w)/2:y=h-text_h-40:")
                        // 해당 이미지 구간에만 표시
                        .append("enable='between(t,")
                        .append(String.format(java.util.Locale.US, "%.3f", t0))
                        .append(",")
                        .append(String.format(java.util.Locale.US, "%.3f", t1))
                        .append(")'");
            }
        }

        // pad 단계 삭제! (여백 없음)
        return draws.length() == 0 ? base : base + "," + draws;
    }


}
