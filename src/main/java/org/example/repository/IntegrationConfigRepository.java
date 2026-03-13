package org.example.repository;

import org.example.dto.data.AutoSyncConfigDto;
import org.example.entity.IntegrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, Long> {

}
