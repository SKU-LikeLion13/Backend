package AiGEN.AiGEN.config;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로(/**)에 대해 CORS 설정 적용
                .allowedOrigins("http://localhost:5173","http://localhost:5174","https://aigen.sku-sku.com") // CORS 요청을 허용할 특정 도메인(origin)을 지정
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드를 지정
                .allowedHeaders("*") // 클라이언트가 모든 HTTP 헤더를 보낼 수 있게 허용
                .allowCredentials(true); // 기본적으로 브라우저는 CORS 요청을 보낼 때 쿠키나 인증 정보를 포함하지 않으므로 true로 설정하여
    }
}