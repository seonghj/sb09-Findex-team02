package org.example.entity;

import jakarta.persistence.*;
import org.hibernate.boot.jaxb.SourceType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Entity
@Table(name = "index_infos")
public class IndexInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "index_name", nullable = false)
    private String indexName;

    @Column(name = "component_count", nullable = false)
    //JPA에서 NULL을 처리하기위해 integer사용
    private Integer component;

    @Column(name = "base_data", nullable = false)
    private OffsetDateTime baseData;

    @Column(name = "base_index", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "favorite", nullable = false)
    private Boolean favorite;













}
