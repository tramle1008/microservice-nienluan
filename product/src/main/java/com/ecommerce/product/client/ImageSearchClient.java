package com.ecommerce.product.client;

import com.ecommerce.product.dto.ImageSearchResponse;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class ImageSearchClient {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8086";

    public ImageSearchClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ImageSearchResponse searchSimilarImages(MultipartFile image) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String url = baseUrl + "/api/image/search?k=20"; // lấy nhiều hơn 1 chút

        try {
            ResponseEntity<ImageSearchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    ImageSearchResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Lỗi khi gọi image search service", e);
            throw new RuntimeException("Không thể tìm kiếm ảnh tương tự", e);
        }
    }
}