package com.insa.projet.microservices.orchestrator_MS.controller;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.insa.projet.microservices.orchestrator_MS.model.nbrPeople.*;
import com.insa.projet.microservices.orchestrator_MS.model.temperature.*;
import com.insa.projet.microservices.orchestrator_MS.model.window.*;

import netscape.javascript.JSObject;

@RestController
public class OrchestratorRessource {
	
	@PostMapping(path="/init/{n}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void initDataBase(@PathVariable int n) {
		RestTemplate restTemplate=new RestTemplate();
		
		restTemplate.postForObject("http://localhost:8080/temperature/init/"+String.valueOf(n), null, List.class);
		restTemplate.postForObject("http://localhost:8080/window/init/"+String.valueOf(n), null, List.class);
		restTemplate.postForObject("http://localhost:8080/nbrPeopleSensor/init/"+String.valueOf(n), null, List.class);

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
	
	@PostMapping(path="/addTemperatureValueRoom/{room}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public TemperatureSensor addTemperatureValue(@RequestBody SensorValue value,@PathVariable int room) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getForObject("http://localhost:8080/temperature/room/"+String.valueOf(room), TemperatureSensor.class).addValue(value);
		return restTemplate.getForObject("http://localhost:8080/temperature/room/"+String.valueOf(room), TemperatureSensor.class);
		
	}
	
	@PostMapping(path="/ManageWindow/{outdoor_threshold}/{delta_temp_threshold}/{threshold_nbrPeople}")
	public void windowManager(@PathVariable double outdoor_threshold,@PathVariable double delta_temp_threshold, @PathVariable int threshold_nbrPeople) {
		
		RestTemplate restTemplate=new RestTemplate();
		List<Window> listWindow;
		List<TemperatureSensor> listTempSensor;
		List<NbrPeopleSensor> listNbrPeopleSensor;

		listWindow=getListWindow();
		listNbrPeopleSensor=getListNbrPeopleSensor();
		listTempSensor=getListTemperatureSensor();
		
		for (int i =0;i<listWindow.size();i++) {
			
			boolean windowState=false;
			
			double outdoorTemp=restTemplate.getForObject("http://localhost:8080/temperature/room/-1", TemperatureSensor.class).getValues().get(-1).getValue();
			int room=listWindow.get(i).getRoom();
			double indoorTemp=restTemplate.getForObject("http://localhost:8080/temperature/room/"+String.valueOf(room), TemperatureSensor.class).getValues().get(-1).getValue();
			int nbrPeople=restTemplate.getForObject("http://localhost:8080/nbrPeopleSensor/room/"+String.valueOf(room), NbrPeopleSensor.class).getValues().get(-1).getNbrPeople();
			
			double delta_temp=Math.abs(outdoorTemp-indoorTemp);
			
			if(outdoorTemp>outdoor_threshold && delta_temp<delta_temp) {
				windowState=true;
				
			}
			
			WindowState ws = new WindowState(windowState, System.currentTimeMillis());
			HttpEntity<WindowState> httpEntity = new HttpEntity<>(ws, null);
			restTemplate.postForObject("http://localhost:8080/window/addStateRoom/"+String.valueOf(room),httpEntity, null);
			
		}
		
	}

}
