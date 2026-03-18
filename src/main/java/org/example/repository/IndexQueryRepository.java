package org.example.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.entity.IndexData;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import static org.example.entity.QIndexData.indexData;
import static org.example.entity.QIndexInfo.indexInfo;

@Repository
@RequiredArgsConstructor
public class IndexQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  public List<IndexData> findDataByDatesAndCategory(List<LocalDate> baseDates, String categoryName){
    return jpaQueryFactory
        .selectFrom(indexData)
        .join(indexData.indexInfo, indexInfo).fetchJoin()
        .where(
            indexData.baseDate.in(baseDates),
            categoryEq(categoryName)
        )
        .fetch();
  }

  public BooleanExpression categoryEq(String categoryName){
    if(!StringUtils.hasText(categoryName)){
      return null;
    }
    return indexInfo.categoryName.eq(categoryName);
  }
}
