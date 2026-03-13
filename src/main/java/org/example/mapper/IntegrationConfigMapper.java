package org.example.mapper;

import org.example.dto.data.AutoSyncConfigDto;
import org.example.entity.IntegrationConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IntegrationConfigMapper {
  AutoSyncConfigDto toDto(IntegrationConfig integrationConfig);
}
