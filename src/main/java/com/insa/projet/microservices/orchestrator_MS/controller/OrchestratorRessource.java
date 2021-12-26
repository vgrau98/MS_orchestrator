package com.insa.projet.microservices.orchestrator_MS.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.insa.projet.microservices.orchestrator_MS.model.nbrPeople.NbrPeopleSensor;
import com.insa.projet.microservices.orchestrator_MS.model.temperature.TemperatureSensor;
import com.insa.projet.microservices.orchestrator_MS.model.window.Window;

@RestController
public class OrchestratorRessource {
	
	@PostMapping(path="/init/{n}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void initDataBase(@PathVariable int n) {
		RestTemplate restTemplate=new RestTemplate();
		
		restTemplate.postForObject("http://localhost:8080/temperature/init/"+String.valueOf(n), null, List.class);
		restTemplate.postForObject("http://localhost:8080/window/init/"+String.valueOf(n), null, List.class);
		restTemplate.postForObject("http://localhost:8080/NbrPeople/init/"+String.valueOf(n), null, List.class);

	}
	
	@GetMapping("/listTemperature")
	public List<TemperatureSensor> getListTemperatureSensor(){
		RestTemplate restTemplate=new RestTemplate();
		List<TemperatureSensor> listSensor;
		listSensor=restTemplate.getForObject("http://localhost:8080/temperature/list", List.class);
		return listSensor;
	}
	
	@GetMapping("/listWindow")
	public List<Window> getListWindow(){
		RestTemplate restTemplate=new RestTemplate();
		List<Window> listWindow;
		listWindow=restTemplate.getForObject("http://localhost:8080/window/list", List.class);
		return listWindow;
	}
	@GetMapping("/listNbrPeopleSensor")
	public List<NbrPeopleSensor> getListNbrPeopleSensor(){
		RestTemplate restTemplate=new RestTemplate();
		List<NbrPeopleSensor> listSensor;
		listSensor=restTemplate.getForObject("http://localhost:8080/nbrPeopleSensor/list", List.class);
		return listSensor;
	}

}
