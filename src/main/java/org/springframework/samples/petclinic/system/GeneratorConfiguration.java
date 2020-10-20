package org.springframework.samples.petclinic.system;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class GeneratorConfiguration {

	@Bean
	public HttpClient httpClient() {
		return HttpClient.newHttpClient();
	}

}
