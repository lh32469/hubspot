package org.gpc4j.hubspot.dto;

import lombok.Data;

import java.util.List;

@Data
public class Partner {

  private String firstName;
  private String lastName;
  private String email;
  private String country;
  private List<String> availableDates;

}
