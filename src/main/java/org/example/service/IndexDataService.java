package org.example.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.RankedIndexPerformanceDto.IndexPerformanceDto;
import org.example.entity.IndexData;
import org.example.repository.IndexDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataService {
  private final IndexDataRepository indexDataRepository;

  public List<IndexPerformanceDto> getFavoritePerformances(List<Long> favoriteIndexIds) {
    Instant today = Instant.now();
    Instant yesterday = today.minus(1, ChronoUnit.DAYS);

    List<Instant> baseDates = List.of(today,yesterday);

    List<IndexData> baseDataList = indexDataRepository
        .findBaseData(favoriteIndexIds, baseDates);

    Map<Long, List<IndexData>> groupedData = baseDataList.stream()
        .collect(Collectors.groupingBy(data -> data.getIndexInfo().getId()));

    return groupedData.entrySet().stream()
        .map(entry -> calculatePerformance(entry.getKey(), entry.getValue()))
        .toList();
  }

  public IndexPerformanceDto calculatePerformance(Long indexId, List<IndexData> dataList){
    if(dataList == null || dataList.isEmpty()) {
      throw new IllegalArgumentException("해당 지수의 데이터가 존재하지 않습니다.");
    }

    IndexData todayData = dataList.get(0);
    BigDecimal todayClosePrice = todayData.getClosePrice();
    BigDecimal yesterdayClosePrice = (dataList.size() > 1) ? dataList.get(1).getClosePrice() : todayClosePrice;

    // 등락폭 계산 (오늘 종가 - 어제 종가)
    BigDecimal priceDiff = todayClosePrice.subtract(yesterdayClosePrice);


    // 등락률 계산 ((등락폭 / 어제 종가) * 100)
    BigDecimal fluctuationRate = (yesterdayClosePrice.compareTo(BigDecimal.ZERO) == 0)
        ? BigDecimal.ZERO // 참이면 등락률 0
        : priceDiff.divide(yesterdayClosePrice, 4, RoundingMode.HALF_UP) // 거짓이면 반올림 해서 소수점 4자리까지
        .multiply(BigDecimal.valueOf(100));

    String indexName = todayData.getIndexInfo().getIndexName();
    String indexClassification = todayData.getIndexInfo().getCategoryName();

    return new IndexPerformanceDto(
        indexId,
        indexClassification,
        indexName,
        priceDiff,
        fluctuationRate,
        todayClosePrice,
        yesterdayClosePrice
    );

  }
}
