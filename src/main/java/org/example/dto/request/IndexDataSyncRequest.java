package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record IndexDataSyncRequest(
    List<Long> indexInfoIds,

   @Schema(description = "대상 날짜 (부터)", example = "2023-01-01")
   LocalDate baseDateFrom,

   @Schema(description = "대상 날짜 (까지)", example = "2023-01-31")
   LocalDate baseDateTo) {
}
