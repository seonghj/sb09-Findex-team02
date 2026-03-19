package org.example.dto.request;

import java.time.LocalDate;
import org.example.entity.type.JobType;
import org.example.entity.type.StatusType;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.format.annotation.DateTimeFormat;

public record SyncJobSearchRequest(

    JobType jobType,
    Long indexInfoId,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDate baseDateFrom,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDate baseDateTo,
    String worker,
    StatusType status,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDate jobTimeFrom,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDate jobTimeTo,

    Long idAfter,
    String cursor,

    String sortField,
    String sortDirection,
    Integer size

) {

  public SyncJobSearchRequest {
    if (size == null)
      size = 50;
  }
}