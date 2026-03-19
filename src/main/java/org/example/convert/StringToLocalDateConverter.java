package org.example.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.core.convert.converter.Converter;

public class StringToLocalDateConverter implements Converter<String, LocalDate> {

  @Override
  public LocalDate convert(String source) {
    if (source == null || source.isBlank()) {
      return null;
    }
    if (source.contains("T")) {
      return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
    }
    return LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE);
  }
}
