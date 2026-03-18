package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지수 정보 목록 조회 요청")
public record IndexInfoSearchRequest(

        @Schema(description = "지수 분류명(부분 일치)", example = "KOSPI")
        String indexClassification,

        @Schema(description = "지수명(부분 일치)", example = "IT")
        String indexName,

        @Schema(description = "즐겨찾기 여부(완전 일치)", example = "true")
        Boolean favorite,

        @Schema(description = "이전 페이지 마지막 요소 ID", example = "10")
        Long idAfter,

        @Schema(description = "다음 페이지 조회를 위한 커서 값", example = "KOSPI시리즈")
        String cursor,

        @Schema(
                description = "정렬 필드",
                example = "indexClassification",
                allowableValues = {"indexClassification", "indexName", "employedItemsCount"}
        )
        String sortField,

        @Schema(
                description = "정렬 방향",
                example = "asc",
                allowableValues = {"asc", "desc"}
        )
        String sortDirection,

        @Schema(description = "페이지 크기", example = "10")
        Integer size

) {
    public IndexInfoSearchRequest {
        if (sortField == null || sortField.isBlank()) {
            sortField = "indexClassification";
        }
        if (sortDirection == null || sortDirection.isBlank()) {
            sortDirection = "asc";
        }
        if (size == null || size <= 0) {
            size = 10;
        }
    }
}