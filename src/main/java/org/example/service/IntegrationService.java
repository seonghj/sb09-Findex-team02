package org.example.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.IndexApiClient;
import org.example.dto.data.SyncJobDto;
import org.example.dto.request.IndexInfoCreateRequest;
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
import org.springframework.transaction.annotation.Propagation;
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
  public List<SyncJobDto> syncIndexInfos(String worker) {
    String baseDateStr = resolveBaseDate();
    List<Item> fetchedItems = fetchAllItems(baseDateStr);

    if (fetchedItems.isEmpty()) {
      log.warn("[지수 정보 연동] API 응답 데이터 없음. 기준일={}", baseDateStr);
      return Collections.emptyList();
    }
    Map<String, IndexInfo> indexInfoMap = indexInfoRepository.findAll().stream()
        .collect(Collectors.toMap(IndexInfo::getIndexName, i -> i));

    List<SyncJobDto> results = new ArrayList<>();

    for (Item item : fetchedItems) {
      SyncJobDto result = processItem(item, indexInfoMap, worker);
      results.add(result);
    }

    return results;
  }

  //건별 트랜잭션 처리
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public SyncJobDto processItem(Item item, Map<String, IndexInfo> indexInfoMap, String worker) {
    IntegrationLog job;
    IndexInfo indexInfo = indexInfoMap.get(item.indexName());

    try {
      if (indexInfo != null) {
        indexInfoService.update(indexInfo.getId(), toIndexInfoUpdateRequest(item));
        job = IntegrationLog.createSuccess(JobType.index_info, indexInfo, Instant.now(), worker);
        log.info("[지수 정보 수정 성공] 이름={}", item.indexName());
      } else {
        IndexInfo created = indexInfoService.create(toIndexInfoCreateRequest(item));
        job = IntegrationLog.createSuccess(JobType.index_info, created, Instant.now(), worker);
        log.info("[지수 정보 등록 성공] 이름={}", item.indexName());
      }
    } catch (Exception e) {
      log.error("[연동 에러] indexName={}, error={}", item.indexName(), e.getMessage());
      job = IntegrationLog.createFailed(JobType.index_info, indexInfo, Instant.now(), worker);
    }

    integrationLogRepository.save(job);
    return syncJobMapper.toDto(job);
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
  //item 데이터 toIndexInfoCreateRequest로 변환
  private IndexInfoCreateRequest toIndexInfoCreateRequest(Item item) {
    return new IndexInfoCreateRequest(
        item.CategoryName(),
        item.indexName(),
        item.componentCount(),
        parseLocalDate(item.infoBaseDate()),
        item.baseIndex(),
        false
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

//기준일 결정
  private String resolveBaseDate() {
    LocalDate today = LocalDate.now();
    String todayStr = today.format(YYYYMMDD);

    OpenApiStockResponseDto probeResponse = indexApiClient.getIndexData(
        serviceKey, 1, 1, todayStr, todayStr, "json"
    );

    if (!extractItems(probeResponse).isEmpty()) {
      return todayStr;
    }

    String yesterdayStr = today.minusDays(1).format(YYYYMMDD);
    log.info("[기준일 fallback] 오늘({}) 데이터 없음 → 전일({}) 사용", todayStr, yesterdayStr);
    return yesterdayStr;
  }

  //페이지네이션으로 전체 데이터 조회
  private List<Item> fetchAllItems(String baseDateStr) {
    List<Item> allItems = new ArrayList<>();
    int pageNo = 1;

    while (true) {
      OpenApiStockResponseDto response = indexApiClient.getIndexData(
          serviceKey, pageNo, PAGE_SIZE, baseDateStr, baseDateStr, "json"
      );
      List<Item> items = extractItems(response);
      allItems.addAll(items);

      if (items.size() < PAGE_SIZE) {
        break;
      }
      pageNo++;
    }

    log.info("[지수 정보 연동] 총 {}건 조회 완료 (기준일={})", allItems.size(), baseDateStr);
    return allItems;
  }

}