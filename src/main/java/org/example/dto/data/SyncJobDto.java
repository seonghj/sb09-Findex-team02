package org.example.dto.data;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SyncJobDto(
    Long id,
    String jobType,
    Long indexInfoId,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate targetDate,
    String worker,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime jobTime,
    String result
) {

}
