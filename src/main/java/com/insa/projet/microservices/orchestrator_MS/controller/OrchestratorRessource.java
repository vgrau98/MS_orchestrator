package com.insa.projet.microservices.orchestrator_MS.controller;

import java.util.List;

import javax.print.attribute.standard.Media;

import org.springframework.beans.factory.annotation.Autowired;
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


@RestController
public class OrchestratorRessource {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@PostMapping(path="/init/{n}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void initDataBase(@PathVariable int n) {
		
		
		restTemplate.postForObject("http://TemperatureSensorsService/temperature/init/"+String.valueOf(n), null, List.class);
		restTemplate.postForObject("http://WindowManagementService/window/init/"+String.valueOf(n), null, List.class);
		restTemplate.postForObject("http://NbrPeopleSensorsService/nbrPeopleSensor/init/"+String.valueOf(n), null, List.class);

	}
	
	@GetMapping(path="/listTemperature", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<TemperatureSensor> getListTemperatureSensor(){
		List<TemperatureSensor> listSensor;
		listSensor=restTemplate.getForObject("http://TemperatureSensorsService/temperature/list", List.class);
		return listSensor;
	}
	
	@GetMapping(path="/listWindow", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Window> getListWindow(){
		List<Window> listWindow;
		listWindow=restTemplate.getForObject("http://WindowManagementService/window/list", List.class);
		return listWindow;
	}
	@GetMapping(path="/listNbrPeopleSensor", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<NbrPeopleSensor> getListNbrPeopleSensor(){
		List<NbrPeopleSensor> listSensor;
		listSensor=restTemplate.getForObject("http://NbrPeopleSensorsService/nbrPeopleSensor/list", List.class);
		return listSensor;
	}
	
	@PostMapping(path="/addTemperatureValueRoom/{room}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addTemperatureValue(@RequestBody SensorValue value,@PathVariable int room) {
		HttpEntity<SensorValue> httpEntity= new HttpEntity<SensorValue>(value, null);
		restTemplate.postForObject("http://TemperatureSensorsService/temperature/addValueRoom/"+String.valueOf(room), value, Object.class);
		
	}
	
	@PostMapping(path="/ManageWindow/{outdoor_threshold}/{delta_temp_threshold}/{threshold_nbrPeople}")
	public void windowManager(@PathVariable double outdoor_threshold,@PathVariable double delta_temp_threshold, @PathVariable int threshold_nbrPeople) {
		
		List<Window> listWindow;
		List<TemperatureSensor> listTempSensor;
		List<NbrPeopleSensor> listNbrPeopleSensor;

		listWindow=getListWindow();
		listNbrPeopleSensor=getListNbrPeopleSensor();
		listTempSensor=getListTemperatureSensor();
		
		for (int i =0;i<listWindow.size();i++) {
			
			boolean windowState=false;
			
			double outdoorTemp=restTemplate.getForObject("http://TemperatureSensorsService/temperature/room/-1", TemperatureSensor.class).getValues().get(-1).getValue();
			int room=listWindow.get(i).getRoom();
			double indoorTemp=restTemplate.getForObject("http://TemperatureSensorsService/temperature/room/"+String.valueOf(room), TemperatureSensor.class).getValues().get(-1).getValue();
			int nbrPeople=restTemplate.getForObject("http://NbrPeopleSensorsService/nbrPeopleSensor/room/"+String.valueOf(room), NbrPeopleSensor.class).getValues().get(-1).getNbrPeople();
			
			double delta_temp=Math.abs(outdoorTemp-indoorTemp);
			
			if(outdoorTemp>outdoor_threshold && delta_temp<delta_temp) {
				windowState=true;
				
			}
			
			WindowState ws = new WindowState(windowState, System.currentTimeMillis());
			HttpEntity<WindowState> httpEntity = new HttpEntity<>(ws, null);
			restTemplate.postForObject("http://WindowManagementService/window/addStateRoom/"+String.valueOf(room),httpEntity, Object.class);
			
		}
		
	}

}
