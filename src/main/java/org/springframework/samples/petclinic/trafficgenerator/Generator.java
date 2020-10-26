package org.springframework.samples.petclinic.trafficgenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.newrelic.api.agent.Trace;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Generator {

  public static final MediaType APPLICATION_JSON = MediaType.parse("application/json");
  private final OkHttpClient httpClient;

  private final ObjectMapper objectMapper;

  private final Random random = new Random();

  public Generator(OkHttpClient httpClient, ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
  }

  @Scheduled(fixedDelay = 1500)
  @Trace(dispatcher = true)
  public void generateSomeTraffic() throws IOException {
    long randomVetId = getRandomVetId();
    List<Owner> owners = getAllOwners();
    Owner randomOwner = owners.get(random.nextInt(owners.size()));
    Owner fullOwner = queryOwnerDeets(randomOwner);
    if (fullOwner.getPets().isEmpty()) {
      return;
    }
    Pet aPet = fullOwner.getPets().get(random.nextInt(fullOwner.getPets().size()));
    Vet vet = getVetById(randomVetId);
    System.out.println("vet = " + vet);
    //todo: can we make an appointment, then maybe delete it?

    if (random.nextInt(100) < 4) {
      postAn404();
    }
    if (random.nextInt(100) < 2) {
      postAnError();
    }

  }

  @Trace
  private void postAn404() throws IOException {
    Call call = httpClient.newCall(
        new Builder()
            .url("http://localhost:8080/nope")
            .post(RequestBody.create("{}", APPLICATION_JSON)).build());
    Response response = call.execute();
    response.body().close();
  }

  @Trace
  private void postAnError() throws IOException {
    System.out.println("posting an error");
    Call call = httpClient.newCall(
        new Builder()
            .url("http://localhost/nope")
            .post(RequestBody.create("{}", APPLICATION_JSON)).build());
    Response response = call.execute();
    response.body().close();
  }

  @Trace
  private Vet getVetById(long vetId) throws IOException {
    String url = "http://localhost:8080/vets/" + vetId;
    String body = get(url);
    return objectMapper.readValue(body, Vet.class);
  }

  @Trace
  private Owner queryOwnerDeets(Owner owner) throws IOException {
    String body = get("http://localhost:8080/owners/" + owner.getId());
    return objectMapper.readValue(body, Owner.class);
  }

  @Trace
  private List<Owner> getAllOwners() throws IOException {
    String body = get("http://localhost:8080/owners/all");
    List<Owner> owners = objectMapper.readValue(body, new TypeReference<List<Owner>>() {
    });
    return owners;
  }

  @Trace
  private long getRandomVetId() throws IOException {
    String body = get("http://localhost:8080/vets");
    JsonNode jsonNode = objectMapper.readTree(body);
    ArrayNode vets = (ArrayNode) jsonNode.get("vetList");
    int size = vets.size();
    int vetToUse = random.nextInt(size);
    JsonNode vet = vets.get(vetToUse);
    return vet.get("id").asLong();
  }

  private String get(String url) throws IOException {

    Call call = httpClient
        .newCall(new Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build());
    Response execute = call.execute();
    ResponseBody responseBody = execute.body();
    String body = responseBody.string();
    responseBody.close();
    return body;
  }
}
