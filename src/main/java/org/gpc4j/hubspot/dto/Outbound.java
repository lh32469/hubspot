package org.gpc4j.hubspot.dto;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Outbound {

  List<Country> countries = new LinkedList<>();


}
