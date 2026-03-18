package org.example.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.example.entity.IndexInfo;
import org.springframework.stereotype.Repository;

@Repository
public class IndexInfoRepositoryImpl implements IndexInfoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<IndexInfo> findIndexInfosByCursor(
            String indexClassification,
            String indexName,
            Boolean favorite,
            String sortField,
            String sortDirection,
            String cursor,
            Long idAfter,
            int size
    ) {
        String entitySortField = mapSortField(sortField);
        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

        StringBuilder jpql = new StringBuilder("SELECT i FROM IndexInfo i WHERE 1=1");

        if (indexClassification != null && !indexClassification.isBlank()) {
            jpql.append(" AND i.categoryName LIKE CONCAT('%', :indexClassification, '%')");
        }

        if (indexName != null && !indexName.isBlank()) {
            jpql.append(" AND i.indexName LIKE CONCAT('%', :indexName, '%')");
        }

        if (favorite != null) {
            jpql.append(" AND i.favorite = :favorite");
        }

        if (cursor != null && !cursor.isBlank() && idAfter != null) {
            if ("categoryName".equals(entitySortField) || "indexName".equals(entitySortField)) {
                if (isAsc) {
                    jpql.append(" AND (i.")
                            .append(entitySortField)
                            .append(" > :cursor OR (i.")
                            .append(entitySortField)
                            .append(" = :cursor AND i.id > :idAfter))");
                } else {
                    jpql.append(" AND (i.")
                            .append(entitySortField)
                            .append(" < :cursor OR (i.")
                            .append(entitySortField)
                            .append(" = :cursor AND i.id > :idAfter))");
                }
            } else if ("component".equals(entitySortField)) {
                if (isAsc) {
                    jpql.append(" AND (i.component > :componentCursor OR (i.component = :componentCursor AND i.id > :idAfter))");
                } else {
                    jpql.append(" AND (i.component < :componentCursor OR (i.component = :componentCursor AND i.id > :idAfter))");
                }
            }
        }

        jpql.append(" ORDER BY i.")
                .append(entitySortField)
                .append(isAsc ? " ASC" : " DESC")
                .append(", i.id ASC");

        TypedQuery<IndexInfo> query = entityManager.createQuery(jpql.toString(), IndexInfo.class);

        if (indexClassification != null && !indexClassification.isBlank()) {
            query.setParameter("indexClassification", indexClassification);
        }

        if (indexName != null && !indexName.isBlank()) {
            query.setParameter("indexName", indexName);
        }

        if (favorite != null) {
            query.setParameter("favorite", favorite);
        }

        if (cursor != null && !cursor.isBlank() && idAfter != null) {
            if ("categoryName".equals(entitySortField) || "indexName".equals(entitySortField)) {
                query.setParameter("cursor", cursor);
                query.setParameter("idAfter", idAfter);
            } else if ("component".equals(entitySortField)) {
                query.setParameter("componentCursor", Integer.valueOf(cursor));
                query.setParameter("idAfter", idAfter);
            }
        }

        query.setMaxResults(size + 1);

        return query.getResultList();
    }

    @Override
    public long countIndexInfos(String indexClassification, String indexName, Boolean favorite) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(i) FROM IndexInfo i WHERE 1=1");

        if (indexClassification != null && !indexClassification.isBlank()) {
            jpql.append(" AND i.categoryName LIKE CONCAT('%', :indexClassification, '%')");
        }

        if (indexName != null && !indexName.isBlank()) {
            jpql.append(" AND i.indexName LIKE CONCAT('%', :indexName, '%')");
        }

        if (favorite != null) {
            jpql.append(" AND i.favorite = :favorite");
        }

        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);

        if (indexClassification != null && !indexClassification.isBlank()) {
            query.setParameter("indexClassification", indexClassification);
        }

        if (indexName != null && !indexName.isBlank()) {
            query.setParameter("indexName", indexName);
        }

        if (favorite != null) {
            query.setParameter("favorite", favorite);
        }

        return query.getSingleResult();
    }

    private String mapSortField(String sortField) {
        return switch (sortField) {
            case "indexClassification" -> "categoryName";
            case "indexName" -> "indexName";
            case "employedItemsCount" -> "component";
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 필드입니다: " + sortField);
        };
    }
}