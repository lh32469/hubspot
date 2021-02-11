package org.gpc4j.hubspot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Data
public class Country {

  private String name;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate startDate;
  private List<String> attendees = new LinkedList<>();

  public long getAttendeeCount() {
    return attendees.size();
  }

}
