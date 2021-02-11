package org.gpc4j.hubspot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gpc4j.hubspot.dto.Country;
import org.gpc4j.hubspot.dto.Inbound;
import org.gpc4j.hubspot.dto.Outbound;
import org.gpc4j.hubspot.dto.Partner;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

  public static final DateTimeFormatter DTF =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");


  static final String SOURCE =
      "https://candidate.hubteam.com/candidateTest/v3/problem/dataset?userKey=1418e02d4f87719640325a686b95";

  static final String DESTINATION =
      "https://candidate.hubteam.com/candidateTest/v3/problem/result?userKey=1418e02d4f87719640325a686b95";

  final static private org.slf4j.Logger LOG
      = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws IOException {
    LOG.info("Running...");

//    ObjectMapper mapper = new ObjectMapper();

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(SOURCE);
    Response response = target.request(MediaType.MEDIA_TYPE_WILDCARD).get();
    LOG.info("response = " + response);

    Inbound inbound = response.readEntity(Inbound.class);
//    InputStream iStream = Main.class.getResourceAsStream("/inbound.json");
//    Inbound inbound = mapper.readValue(iStream, Inbound.class);

//    LOG.info("inbound = " + inbound);

    final List<Partner> partners = inbound.getPartners();

    // Get Country names
    Set<String> countryNames = partners.stream()
        .map(p -> p.getCountry())
        .collect(Collectors.toSet());

    LOG.debug("countryNames = " + countryNames);

    // Possible dates for each country
    final Map<String, Set<LocalDate>> possibleDates = new HashMap<>();

    // Possible Partners for each country
    final Map<String, List<Partner>> partnersByCountry = new HashMap<>();

    for (Partner partner : partners) {
      final String countryName = partner.getCountry();

      // Initialize country for available dates
      possibleDates.putIfAbsent(countryName, new HashSet<>());

      Set<LocalDate> dates = possibleDates.get(countryName);
      for (String availableDate : partner.getAvailableDates()) {
        dates.add(LocalDate.parse(availableDate, DTF));
      }

      // Initialize partnerList for country
      partnersByCountry.putIfAbsent(countryName, new LinkedList<>());
      List<Partner> partnerList = partnersByCountry.get(countryName);
      partnerList.add(partner);

    }

    List<Country> countries = new LinkedList<>();

    for (String countryName : possibleDates.keySet()) {
//      LOG.info(countryName + "  = " + possibleDates.get(countryName));
      for (LocalDate value : possibleDates.get(countryName)) {
        Country country = new Country();
        country.setName(countryName);
        country.setStartDate(value);
        countries.add(country);
      }
    }


//    LOG.info("countries = " + countries);
//    LOG.info("countries.size() = " + countries.size());
//    LOG.info("partners.size() = " + partners.size());

    // Calculate how many attendees can attend each possible Country/Date combination
    for (Country country : countries) {
      // Get Partners for this country
      List<Partner> pList = partnersByCountry.get(country.getName());
      //LOG.info(country.getName() + " partners = " + pList);
      for (Partner partner : pList) {
        List<String> partnerAvailableDates = partner.getAvailableDates();
        String startDate = country.getStartDate().toString();
        String endDate = country.getStartDate().plusDays(1).toString();
        if (partnerAvailableDates.contains(startDate) &&
            partnerAvailableDates.contains(endDate)) {
//          LOG.info("Possible Attendee = " + partner);
          country.getAttendees().add(partner.getEmail());
        }
      }
    }

    // For each country, get the highest attendance event
    Outbound outbound = new Outbound();
    for (String countryName : countryNames) {
      Country best = countries.parallelStream()
          .filter(c -> c.getName().equals(countryName))
          .reduce((c1, c2) -> {
            if (c1.getAttendees().size() > c2.getAttendees().size()) {
              return c1;
            } else if (c2.getAttendees().size() > c1.getAttendees().size()) {
              return c2;
            } else if (c1.getStartDate().isBefore(c2.getStartDate())) {
              return c1;
            }
            return c2;
          }).get();
      outbound.getCountries().add(best);
    }

    // Set startDate to null for Countries with no attendees
    outbound.getCountries().forEach(c -> {
      if (c.getAttendees().isEmpty()) {
        c.setStartDate(null);
      }
    });

//    mapper.registerModule(new JavaTimeModule());
//    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
//    final String json = writer.writeValueAsString(outbound);
//
//    System.out.println(json);

    LOG.info("Processing complete.");
    client = ClientBuilder.newClient();
    target = client.target(DESTINATION);
    Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
    response = invocationBuilder.post(Entity.entity(outbound, MediaType.APPLICATION_JSON));

    LOG.info("response = " + response);
  }


}
