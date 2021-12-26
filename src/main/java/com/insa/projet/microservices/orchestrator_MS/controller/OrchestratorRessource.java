package com.insa.projet.microservices.orchestrator_MS.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.insa.projet.microservices.orchestrator_MS.model.temperature.TemperatureSensor;

@RestController
public class OrchestratorRessource {
	
	@PostMapping(path="/init/{n}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void initDataBase(@PathVariable int n) {
		RestTemplate restTemplate=new RestTemplate();
		
		restTemplate.postForObject("http://localhost:8080/temperature/init/"+String.valueOf(n), null, null);
	}
	
	@GetMapping("/list")
	public List<TemperatureSensor> getListTemperatureSensor(){
		RestTemplate restTemplate=new RestTemplate();
		List<TemperatureSensor> listSensor;
		listSensor=restTemplate.getForObject("http://localhost:8080/temperature/list", List.class);
		return listSensor;
	}

}
