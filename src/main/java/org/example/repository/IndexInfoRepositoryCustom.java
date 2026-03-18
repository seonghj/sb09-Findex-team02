package org.example.repository;

import java.util.List;
import org.example.entity.IndexInfo;

public interface IndexInfoRepositoryCustom {

    List<IndexInfo> findIndexInfosByCursor(
            String indexClassification,
            String indexName,
            Boolean favorite,
            String sortField,
            String sortDirection,
            String cursor,
            Long idAfter,
            int size
    );

    long countIndexInfos(
            String indexClassification,
            String indexName,
            Boolean favorite
    );
}