package org.gpc4j.hubspot.dto;

import lombok.Data;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Data
public class Outbound {

  @Getter(lazy = true)
  private final List<Country> countries = new LinkedList<>();


}
