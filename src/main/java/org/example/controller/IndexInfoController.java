package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.dto.request.IndexInfoSearchRequest;
import org.example.dto.request.IndexInfoUpdateRequest;
import org.example.dto.response.CursorPageResponseIndexInfoDto;
import org.example.dto.response.IndexInfoResponseDto;
import org.example.dto.response.IndexInfoSummaryDto;
import org.example.service.IndexInfoService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지수 정보 API")
@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

    private final IndexInfoService indexInfoService;

    /**
     * 지수 정보 등록
     */
    @Operation(summary = "지수 정보 등록" , description = "새로운 지수 정보를 등록합니다.")
    @PostMapping
    public ResponseEntity<IndexInfoResponseDto> createIndexInfo(
            @RequestBody IndexInfoCreateRequest request
    ) {
        IndexInfoResponseDto response = indexInfoService.createIndexInfo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 지수 정보 단건 조회
     */
    @Operation(summary = "지수 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<IndexInfoResponseDto> findIndexInfoById(@PathVariable Long id) {
        return ResponseEntity.ok(indexInfoService.findIndexInfoById(id));
    }

    /**
     * 지수 정보 요약 목록 조회
     */
    @Operation(summary = "지수 정보 요약 목록 조회")
    @GetMapping("/summaries")
    public ResponseEntity<List<IndexInfoSummaryDto>> findIndexInfoSummaries() {
        return ResponseEntity.ok(indexInfoService.findIndexInfoSummaries());
    }

    /**
     * 지수 정보 목록 조회
     */
    @Operation(summary = "지수 정보 목록 조회", description = "지수 정보 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
    @GetMapping
    public ResponseEntity<CursorPageResponseIndexInfoDto<IndexInfoResponseDto>> findIndexInfosByCursor(
            @ParameterObject IndexInfoSearchRequest request
    ) {
        return ResponseEntity.ok(indexInfoService.findIndexInfosByCursor(request));
    }

    /**
     * 지수 정보 수정
     */
    @Operation(summary = "지수 정보 수정", description = "기존 지수 정보를 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<IndexInfoResponseDto> updateIndexInfo(
        @Schema(description = "지수 정보 ID")
        @PathVariable Long id,
        @RequestBody IndexInfoUpdateRequest request
    ) {
        IndexInfoResponseDto response = indexInfoService.updateIndexInfo(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 지수 정보 삭제
     * IndexInfo 삭제 시 연관된 IndexData도 함께 삭제됨
     */
    @Operation(summary = "지수 정보 삭제", description = "지수 정보를 삭제합니다. 관련된 지수 데이터도 함께 삭제됩니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexInfo(
        @Schema(description = "지수 정보 ID")
        @PathVariable Long id) {
        indexInfoService.deleteIndexInfo(id);
        return ResponseEntity.noContent().build();
    }
}