package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.dto.request.IndexInfoSearchRequest;
import org.example.dto.request.IndexInfoUpdateRequest;
import org.example.dto.response.CursorPageResponseIndexInfoDto;
import org.example.dto.response.IndexInfoResponseDto;
import org.example.dto.response.IndexInfoSummaryDto;
import org.example.entity.IndexInfo;
import org.example.repository.IndexInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

    private final IndexInfoRepository indexInfoRepository;
    private final AutoSyncConfigService autoSyncConfigService;

    @Transactional
    public IndexInfoResponseDto createIndexInfo(IndexInfoCreateRequest request) {

        boolean exists = indexInfoRepository.existsByCategoryNameAndIndexName(
                request.indexClassification(),
                request.indexName()
        );

        if (exists) {
            throw new IllegalArgumentException("이미 같은 지수 분류명과 지수명을 가진 지수 정보가 존재합니다.");
        }

        IndexInfo indexInfo = new IndexInfo(
                request.indexClassification(),
                request.indexName(),
                request.sourceType()
        );

        indexInfo.setIndexDetails(
                request.basePointInTime(),
                request.baseIndex(),
                request.employedItemsCount()
        );

        if (request.favorite() != null) {
            indexInfo.updateFavorite(request.favorite());
        }

        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

        autoSyncConfigService.create(savedIndexInfo.getId());

        return IndexInfoResponseDto.from(savedIndexInfo);
    }

    @Transactional(readOnly = true)
    public IndexInfoResponseDto findIndexInfoById(Long id) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        return IndexInfoResponseDto.from(indexInfo);
    }

    @Transactional(readOnly = true)
    public List<IndexInfoSummaryDto> findIndexInfoSummaries() {
        return indexInfoRepository.findAll().stream()
                .map(indexInfo -> new IndexInfoSummaryDto(
                        indexInfo.getId(),
                        indexInfo.getCategoryName(),
                        indexInfo.getIndexName()
                ))
                .toList();
    }

    @Transactional
    public IndexInfoResponseDto updateIndexInfo(Long id, IndexInfoUpdateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        Integer component = request.employedItemsCount() != null
                ? request.employedItemsCount()
                : indexInfo.getComponent();

        LocalDate baseDate = request.basePointInTime() != null
                ? request.basePointInTime()
                : indexInfo.getBaseDate();

        BigDecimal baseIndex = request.baseIndex() != null
                ? request.baseIndex()
                : indexInfo.getBaseIndex();

        indexInfo.setIndexDetails(baseDate, baseIndex, component);

        if (request.favorite() != null) {
            indexInfo.updateFavorite(request.favorite());
        }

        return IndexInfoResponseDto.from(indexInfo);
    }

    @Transactional
    public void deleteIndexInfo(Long id) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        indexInfoRepository.delete(indexInfo);
    }

    @Transactional(readOnly = true)
    public CursorPageResponseIndexInfoDto<IndexInfoResponseDto> findIndexInfosByCursor(IndexInfoSearchRequest request) {

        validateSortField(request.sortField());
        validateSortDirection(request.sortDirection());

        List<IndexInfo> indexInfos = indexInfoRepository.findIndexInfosByCursor(
                request.indexClassification(),
                request.indexName(),
                request.favorite(),
                request.sortField(),
                request.sortDirection(),
                request.cursor(),
                request.idAfter(),
                request.size()
        );

        boolean hasNext = indexInfos.size() > request.size();
        List<IndexInfo> content = hasNext
                ? indexInfos.subList(0, request.size())
                : indexInfos;

        List<IndexInfoResponseDto> contentDto = content.stream()
                .map(IndexInfoResponseDto::from)
                .toList();

        Long nextIdAfter = null;
        String nextCursor = null;

        if (hasNext && !content.isEmpty()) {
            IndexInfo last = content.get(content.size() - 1);
            nextIdAfter = last.getId();
            nextCursor = extractCursorValue(last, request.sortField());
        }

        long totalElements = indexInfoRepository.countIndexInfos(
                request.indexClassification(),
                request.indexName(),
                request.favorite()
        );

        return new CursorPageResponseIndexInfoDto<>(
                contentDto,
                nextCursor,
                nextIdAfter,
                contentDto.size(),
                totalElements,
                hasNext
        );
    }

    private void validateSortField(String sortField) {
        if (!"indexClassification".equals(sortField)
                && !"indexName".equals(sortField)
                && !"employedItemsCount".equals(sortField)) {
            throw new IllegalArgumentException("정렬 필드는 indexClassification, indexName, employedItemsCount 중 하나여야 합니다.");
        }
    }

    private void validateSortDirection(String sortDirection) {
        if (!"asc".equalsIgnoreCase(sortDirection) && !"desc".equalsIgnoreCase(sortDirection)) {
            throw new IllegalArgumentException("정렬 방향은 asc 또는 desc만 가능합니다.");
        }
    }

    private String extractCursorValue(IndexInfo indexInfo, String sortField) {
        return switch (sortField) {
            case "indexClassification" -> indexInfo.getCategoryName();
            case "indexName" -> indexInfo.getIndexName();
            case "employedItemsCount" -> String.valueOf(indexInfo.getComponent());
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 필드입니다: " + sortField);
        };
    }
}