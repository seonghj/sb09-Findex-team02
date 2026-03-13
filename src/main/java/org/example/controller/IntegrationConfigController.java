package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.data.AutoSyncConfigDto;
import org.example.dto.request.AutoSyncConfigUpdateRequest;
import org.example.service.IntegrationConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auto-sync-configs")
public class IntegrationConfigController {
  private final IntegrationConfigService integrationConfigService;

  @GetMapping("/{configId}")
  ResponseEntity<AutoSyncConfigDto> findById(@PathVariable Long configId){
    AutoSyncConfigDto autoSyncConfigDto = integrationConfigService.find(configId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(autoSyncConfigDto);
  }

  @PatchMapping(path = "/{configId}")
  public ResponseEntity<AutoSyncConfigDto> updateUserStatusByUserId(@PathVariable Long configId,
      @RequestBody AutoSyncConfigUpdateRequest request) {
    AutoSyncConfigDto autoSyncConfigStatus = integrationConfigService.update(configId, request);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(autoSyncConfigStatus);
  }

}
