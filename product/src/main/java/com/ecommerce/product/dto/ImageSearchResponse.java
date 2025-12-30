package com.ecommerce.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ImageSearchResponse(
        String query,

        @JsonProperty("results_count")
        Integer resultsCount,

        @JsonProperty("similar_images")
        List<SimilarImage> similarImages
) {
    // Class con để map từng ảnh
    public record SimilarImage(
            String filename,
            Double similarity,

            @JsonProperty("score_percent")
            Double scorePercent
    ) {}
}