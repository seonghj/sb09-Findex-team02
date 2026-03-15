package org.example.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.IndexApiClient;
import org.example.dto.data.SyncJobDto;
import org.example.dto.request.IndexInfoUpdateRequest;
import org.example.dto.response.OpenApiStockResponseDto;
import org.example.dto.response.OpenApiStockResponseDto.Item;
import org.example.entity.IndexInfo;
import org.example.entity.IntegrationLog;
import org.example.entity.type.JobType;
import org.example.mapper.SyncJobMapper;
import org.example.repository.IndexInfoRepository;
import org.example.repository.IntegrationLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

  private final IntegrationLogRepository integrationLogRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexApiClient indexApiClient;
  private final IndexInfoService indexInfoService;
  private final SyncJobMapper syncJobMapper;

  @Value("${openapi.service-key}")
  private String serviceKey;
  private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final int PAGE_SIZE = 500;

  //자수 정보 연동
  @Transactional
  public List<SyncJobDto> syncIndexInfos(String worker) {
    List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();
    LocalDate today = LocalDate.now();
    String todayStr = today.format(YYYYMMDD);
    List<SyncJobDto> results = new ArrayList<>();

    OpenApiStockResponseDto response = indexApiClient.getIndexData(
        serviceKey, 1, 100, todayStr, todayStr, "json" // 넉넉하게 100건 조회
    );
    List<Item> fetchedItems = extractItems(response);

    for (IndexInfo indexInfo : allIndexInfos) {
      IntegrationLog job;
      try {
        Item matchedItem = findMatchingItem(fetchedItems, indexInfo.getIndexName()).orElse(null);

        if (matchedItem != null) {
          indexInfoService.update(indexInfo.getId(), toIndexInfoUpdateRequest(matchedItem));
          job = IntegrationLog.createSuccess(JobType.index_info, indexInfo, Instant.now(), worker);
        } else {
          log.warn("[지수 정보 매칭 실패] 이름={}", indexInfo.getIndexName());
          job = IntegrationLog.createFailed(JobType.index_info, indexInfo, Instant.now(), worker);
        }

      } catch (Exception e) {
        job = IntegrationLog.createFailed(JobType.index_info, indexInfo, Instant.now(), worker);
        log.error("[연동 에러] indexInfoId={}, error={}", indexInfo.getId(), e.getMessage());
      }

      integrationLogRepository.save(job);
      results.add(syncJobMapper.toDto(job));
    }
    return results;
  }


//API 응답에서 Item 리스트 추출
  private List<Item> extractItems(OpenApiStockResponseDto response) {
    return Optional.ofNullable(response)
        .map(OpenApiStockResponseDto::response)
        .map(OpenApiStockResponseDto.Response::body)
        .map(OpenApiStockResponseDto.Body::items)
        .map(OpenApiStockResponseDto.Items::item)
        .orElse(Collections.emptyList());
  }
  //이름이 일치하는 주가 찾기
  private Optional<Item> findMatchingItem(List<Item> items, String indexName) {
    return items.stream()
        .filter(item -> indexName.equals(item.indexName()))
        .findFirst();
  }
  //item 데이터 toIndexInfoUpdateRequest로 변환
  private IndexInfoUpdateRequest toIndexInfoUpdateRequest(Item item) {
    return new IndexInfoUpdateRequest(
        item.componentCount(),
        parseLocalDate(item.infoBaseDate()),
        item.baseIndex(),
        null
    );
  }
  //날짜(문자열 -> LocalDate로 변환)
  private LocalDate parseLocalDate(String dateStr) {

    if (dateStr == null || dateStr.isBlank()) {
      return null;
    }

    try {
      return LocalDate.parse(dateStr, YYYYMMDD);
    } catch (DateTimeParseException e) {
      log.warn("날짜 파싱 실패: {}", dateStr);
      return null;
    }
  }
}