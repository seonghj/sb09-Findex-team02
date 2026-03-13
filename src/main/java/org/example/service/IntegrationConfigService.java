package org.example.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.AutoSyncConfigDto;
import org.example.dto.request.AutoSyncConfigUpdateRequest;
import org.example.entity.IndexInfo;
import org.example.entity.IntegrationConfig;
import org.example.mapper.IntegrationConfigMapper;
import org.example.repository.IndexInfoRepository;
import org.example.repository.IntegrationConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntegrationConfigService {
  private final IntegrationConfigRepository integrationConfigRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IntegrationConfigMapper integrationConfigMapper;

  public AutoSyncConfigDto create(Long indexId){

    // index_id 값으로 index_info 객체 조회
    IndexInfo indexInfoProxy = indexInfoRepository.getReferenceById(indexId);
    IntegrationConfig newIntegrationConfig = new IntegrationConfig(indexInfoProxy);

    integrationConfigRepository.save(newIntegrationConfig);

    return integrationConfigMapper.toDto(newIntegrationConfig);
  }

  public AutoSyncConfigDto find(Long id){
    return integrationConfigMapper.toDto(integrationConfigRepository.findById(id).orElseThrow());
  }

  public List<AutoSyncConfigDto> findAll(){
    return integrationConfigRepository.findAll().stream()
        .map(integrationConfigMapper::toDto)
        .toList();
  }

  public AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request){
    IntegrationConfig config = integrationConfigRepository.findById(id).orElseThrow();
    config.updateEnabled(request.enabled());
    return integrationConfigMapper.toDto(config);

  }

  public void delete(Long id){
    integrationConfigRepository.deleteById(id);
  }
}
