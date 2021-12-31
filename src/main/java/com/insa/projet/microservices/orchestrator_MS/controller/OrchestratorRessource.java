package com.insa.projet.microservices.orchestrator_MS.controller;

import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.Media;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
		RestTemplate restTemplate = new RestTemplate();
		listSensor=restTemplate.getForObject("http://TemperatureSensorsService/list", List.class);
		//listSensor=restTemplate.getForObject("http://TemperatureSensorsService/temperature/list", List.class);
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
	
	@PostMapping(path="/addTemperatureValueRoom/{room}", produces=MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addTemperatureValue(@RequestBody SensorValue value,@PathVariable int room) {
		//RestTemplate restTemplate=new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<SensorValue> httpEntity= new HttpEntity<SensorValue>(value, headers);
		restTemplate.postForObject("http://TemperatureSensorsService/temperature/addValueRoom/"+String.valueOf(room), httpEntity, Object.class);
		
	}
	
	@GetMapping(path="/updateTempDB")
	public void updateTempDB() {
		
		RestTemplate restTemplateLocal=new RestTemplate();
		JSONArray listUriSensors=new JSONArray();
		
		ResponseEntity<String> response;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("X-M2M-Origin", "admin:admin");
		headers.set("Content-Tyoe","application/json");
		headers.set("accept", "application/json");
		
		HttpEntity<Void> httpEntity= new HttpEntity<>(headers);
		
		response=restTemplateLocal.exchange("http://localhost:8082/~/in-cse/in-name/?fu=1&ty=2&lbl=SensorType/Temperature", HttpMethod.GET, httpEntity, String.class);
		
		JSONObject content=new JSONObject(response.getBody().substring(response.getBody().indexOf('{'), response.getBody().indexOf('}')+1));
		listUriSensors= (JSONArray) content.get("m2m:uril");
		for(int i=0;i<listUriSensors.length();i++) {
			response=restTemplateLocal.exchange("http://localhost:8082/~"+listUriSensors.getString(i)+"/DESCRIPTOR/la", HttpMethod.GET, httpEntity, String.class);
			JSONObject descriptor=new JSONObject(response.getBody().substring(response.getBody().indexOf('{'), response.getBody().indexOf('}')+1));
			JSONObject value_descriptor=new JSONObject(descriptor.getJSONObject("m2m:cin").getJSONObject("con"));
			System.out.println(response);
			
			if(!restTemplate.getForObject("http://TemperatureSensorsService/temperature/sensorInID/"+String.valueOf(value_descriptor.getInt("ID")), boolean.class)) {
				headers.setContentType(MediaType.APPLICATION_JSON);
				TemperatureSensor sensor = new TemperatureSensor(value_descriptor.getInt("ID"), value_descriptor.getInt("room"));
				restTemplate.postForObject("http://TemperatureSensorsService/temperature/addSensor/", new HttpEntity<TemperatureSensor>(sensor, headers), String.class);
			}
		}
		
		System.out.println(content.get("m2m:uril").getClass());
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
