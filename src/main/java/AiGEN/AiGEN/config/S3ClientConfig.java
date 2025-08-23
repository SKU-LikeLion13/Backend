package AiGEN.AiGEN.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;


@Configuration
@RequiredArgsConstructor
public class S3ClientConfig {
    private final R2Properties props;

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
                        )
                )
                // 리전은 의미 거의 없지만 필수 파라미터라 아무거나 지정
                .region(Region.US_EAST_1)
                // R2는 path-style 권장
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                // R2 엔드포인트로 덮어쓰기
                .endpointOverride(URI.create(props.getEndpoint()))
                .build();
    }
}
