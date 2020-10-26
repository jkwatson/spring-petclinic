package org.springframework.samples.petclinic.system;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class GeneratorConfiguration {

  @Bean
  public OkHttpClient httpClient() {
    return new OkHttpClient();
  }

}
