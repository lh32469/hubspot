package org.gpc4j.hubspot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;

public class Main {

  private static final String URL =
      "https://client.schwab.com/public/quickquote/psqqset.ashx?symbol=";


  final static private org.slf4j.Logger LOG
      = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws IOException {
    LOG.info("args = " + Arrays.asList(args));

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(URL + "NVG");
    Response response = target.request(MediaType.MEDIA_TYPE_WILDCARD).get();

    String entity = response.readEntity(String.class);
    LOG.info("entity = " + entity);
    entity = entity.replaceFirst("QQ.Set\\(", "");
    entity = entity.replaceFirst("}]}\\)", "}]}");

    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.reader().readTree(entity);
    LOG.info("node = " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));

    ArrayNode symbols = (ArrayNode) node.get("Symbols");
    JsonNode symNode = symbols.get(0);
    LOG.info("symNode = " + symNode.asText());
  }

}
