package com.insa.projet.microservices.orchestrator_MS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class OrchestratorMsApplication {
	
	@Bean
	@LoadBalanced
	public RestTemplate restTempate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(OrchestratorMsApplication.class, args);
	}

}

