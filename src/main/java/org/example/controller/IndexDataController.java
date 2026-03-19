package org.example.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.ByteArrayInputStream;
import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexChartDto;
import org.example.dto.data.IndexDataDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataSearchRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.dto.response.CursorPageResponseIndexDataDto;
import org.example.dto.response.FavoritePerformanceResponse;
import org.example.dto.response.RankedIndexPerformanceDto;
import org.example.service.IndexDataService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  // 생성 (POST)
  @Operation(summary = "지수 데이터 등록", description = "새로운 지수 데이터를 등록합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "지수 데이터 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 데이터 값 등)"),
      @ApiResponse(responseCode = "404", description = "참조하는 지수 정보를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping
  public Long create(@RequestBody IndexDataCreateRequest request) {


    return indexDataService.create(request);

  }

  // 목록 조회 (GET)
  @Operation(summary = "지수 데이터 목록 조회", description = "지수 데이터 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "지수 데이터 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 데이터 값 등)"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public CursorPageResponseIndexDataDto<IndexDataDto> search(
      @ModelAttribute IndexDataSearchRequest request
  ) {
    return indexDataService.search(request);
  }

  // 수정 (PATCH)
  @Operation(summary = "지수 데이터 수정", description = "기존 지수 데이터를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "지수 데이터 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 데이터 값 등)"),
      @ApiResponse(responseCode = "404", description = "수정할 지수 데이터를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PatchMapping("/{id}")
  public Long update(
      @PathVariable Long id,
      @RequestBody LocalDate baseDate,
      @RequestBody IndexDataUpdateRequest request
  ) {
    return indexDataService.update(id, baseDate, request);
  }

  // 삭제 (DELETE)
  @Operation(summary = "지수 데이터 삭제", description = "지수 데이터를 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "지수 데이터 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "삭제할 지수 데이터를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @DeleteMapping("/{id}")
  public void delete(
      @PathVariable Long id,
      @RequestBody LocalDate baseDate
  ) {
    indexDataService.delete(id, baseDate);
  }


  @Operation(summary = "관심 지수 성과 조회", description = "즐겨찾기한 지수들의 기간별 성과를 요약하여 조회합니다.")
  @GetMapping("/performance/favorite")
  public ResponseEntity<List<FavoritePerformanceResponse>> getFavoriteIndexRankings(
      @Schema(allowableValues = {"DAILY", "WEEKLY", "MONTHLY"},
          description = "성과 기간 유형 (DAILY, WEEKLY, MONTHLY)\n"
              + "\n" + "Default value : DAILY")
      @RequestParam String periodType
  ) {
    return ResponseEntity.ok(indexDataService.getFavoritePerformances(periodType));
  }

  @Operation(summary = "지수 데이터를 CSV export", description = "지수 데이터를 CSV 파일로 export합니다.")
  @GetMapping("/export/csv")
  public ResponseEntity<InputStreamResource> export(
      @ModelAttribute IndexDataSearchRequest request
  ) {

    ByteArrayInputStream csv = indexDataService.export(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=index-data.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(new InputStreamResource(csv));
  }


  @Operation(summary = "지수 성과 랭킹 조회", description = "지수의 성과 분석 랭킹을 조회합니다.")
  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getRankingPerformance(
      @Schema(description = "지수 정보")
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(required = false) String indexName,
      @Schema(allowableValues = {"DAILY", "WEEKLY", "MONTHLY"},
          description = "성과 기간 유형 (DAILY, WEEKLY, MONTHLY)\n"
              + "\n" + "Default value : DAILY")
      @RequestParam(defaultValue = "DAILY") String periodType,
      @Schema(description = "최대 랭킹 수")
      @RequestParam(defaultValue = "10") Integer rankLimit
  ) {
    List<RankedIndexPerformanceDto> result = indexDataService.getPerformanceRanking(indexInfoId,
        indexName, periodType, rankLimit);
    return ResponseEntity.ok(result);
  }


  @Operation(summary = "지수 차트 조회", description = "지수 차트 데이터를 조회합니다.")
  @GetMapping("{id}/chart")
  public ResponseEntity<List<IndexChartDto>> getIndexChart(
      @Schema(description = "지수 정보 ID")
      @PathVariable(required = false) Long id,
      @RequestParam(required = false) String indexName,
      @Schema(allowableValues = {"MONTHLY", "QUARTERLY", "YEARLY"},
          description = "성과 기간 유형 (DAILY, WEEKLY, MONTHLY)\n"
              + "\n" + "Default value : DAILY"  )
      @RequestParam(defaultValue = "MONTHLY") String periodType
  ) {
    List<IndexChartDto> indexCharList = indexDataService.getIndexChart(id, indexName,
        periodType);
    return ResponseEntity.ok(indexCharList);
  }
}