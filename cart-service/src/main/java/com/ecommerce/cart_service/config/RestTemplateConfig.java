package com.ecommerce.cart_service.config;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced  // BẮT BUỘC: để gọi qua Eureka[](http://product-service)
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Interceptor: copy X-User-Id từ request hiện tại → sang request nội bộ
        restTemplate.getInterceptors().add((request, body, execution) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest currentRequest = attributes.getRequest();
                String userId = currentRequest.getHeader("X-User-Id");
                if (userId != null) {
                    request.getHeaders().add("X-User-Id", userId);
                }
            }
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}