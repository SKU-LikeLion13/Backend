package AiGEN.AiGEN.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "r2")
public class R2Properties {
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String bucket;
    private String publicBase;  //공개url
    private String region;

}
