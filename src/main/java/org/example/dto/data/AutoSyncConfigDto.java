package org.example.dto.data;


import com.fasterxml.jackson.annotation.JsonProperty;

public record AutoSyncConfigDto(
    Long id,
    Long indexInfoId,
    String indexClassification,
    Boolean enabled
) {}
