package org.springframework.samples.petclinic.trafficgenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Random;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Generator {

	private final VetRepository vetRepository;

	private final OwnerRepository ownerRepository;

	private final PetRepository petRepository;

	private final HttpClient httpClient;

	private final ObjectMapper objectMapper;

	private final Random random = new Random();

	public Generator(VetRepository vetRepository, OwnerRepository ownerRepository, PetRepository petRepository,
			HttpClient httpClient, ObjectMapper objectMapper) {
		this.vetRepository = vetRepository;
		this.ownerRepository = ownerRepository;
		this.petRepository = petRepository;
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	@Scheduled(fixedDelay = 1500)
	public void generateSomeTraffic() throws IOException, InterruptedException {
		System.out.println("querying");
		long randomVetId = getRandomVetId();
		List<Owner> owners = getAllOwners();
		Owner randomOwner = owners.get(random.nextInt(owners.size()));
		Owner fullOwner = queryOwnerDeets(randomOwner);
		if (fullOwner.getPets().isEmpty()) {
			return;
		}
		Pet aPet = fullOwner.getPets().get(random.nextInt(fullOwner.getPets().size()));
		Vet vet = getVetById(randomVetId);
	}

	private Vet getVetById(long vetId) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder().GET().header("Accept", "application/json")
				.uri(URI.create("http://localhost:8080/vets/" + vetId)).build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		String body = response.body();
		return objectMapper.readValue(body, Vet.class);
	}

	private Owner queryOwnerDeets(Owner owner) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder().GET().header("Accept", "application/json")
				.uri(URI.create("http://localhost:8080/owners/" + owner.getId())).build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		String body = response.body();
		return objectMapper.readValue(body, Owner.class);
	}

	private List<Owner> getAllOwners() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8080/owners/all"))
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		String body = response.body();
		List<Owner> owners = objectMapper.readValue(body, new TypeReference<List<Owner>>() {
		});
		return owners;
	}

	private long getRandomVetId() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8080/vets")).build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		String body = response.body();
		JsonNode jsonNode = objectMapper.readTree(body);
		ArrayNode vets = (ArrayNode) jsonNode.get("vetList");
		int size = vets.size();
		int vetToUse = random.nextInt(size);
		JsonNode vet = vets.get(vetToUse);
		long vetId = vet.get("id").asLong();
		return vetId;
	}

}
