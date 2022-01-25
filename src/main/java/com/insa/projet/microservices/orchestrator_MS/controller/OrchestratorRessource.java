package com.insa.projet.microservices.orchestrator_MS.controller;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Path;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.insa.projet.microservices.orchestrator_MS.model.nbrPeople.*;
import com.insa.projet.microservices.orchestrator_MS.model.temperature.*;
import com.insa.projet.microservices.orchestrator_MS.model.window.*;

/**
 * 
 * @author grau
 * Exposes the key resources for the application. Uses three microservices to manage a database of temperature and people sensors and window actuators.
 * The databases are built from the OM2M platform. 
 */
@RestController
public class OrchestratorRessource {

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * As soon as a new resource is created on OM2M, the orchestrator subscribes. 
	 * This data structure has as key the uri of the subscription associated to the created resource (JSON object with id and room)
	 */
	Map<String, JSONObject> subRessources = new HashMap<String, JSONObject>();
	
	/**
	 * As soon as a new window actuator is created on OM2M, the orchestrator is notified. 
	 * This data structure has as key the uri of the window actuator on OM2M associated to the id and room of the window actuator
	 */
	Map<String, String> uriWindows = new HashMap<String, String>();

	private double outdoorTempThreshold = 18;
	private int nbrPeopleThreshold = 1;
	private double deltaTempThredhold = 5;
	private boolean autoManagement = true;

	/**
	 * 
	 * @return list of temperature sensors
	 */
	@GetMapping(path = "/listTemperature", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TemperatureSensor> getListTemperatureSensor() {

		List<TemperatureSensor> listSensor;

		ResponseEntity<List<TemperatureSensor>> response = restTemplate.exchange(
				"http://temperatureSensorsService/temperature/list", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<TemperatureSensor>>() {
				});
		listSensor = response.getBody();
		return listSensor;
	}

	/**
	 * 
	 * @return list of window actuators
	 */
	@GetMapping(path = "/listWindow", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Window> getListWindow() {
		List<Window> listWindow;

		ResponseEntity<List<Window>> response = restTemplate.exchange("http://WindowManagementService/window/list",
				HttpMethod.GET, null, new ParameterizedTypeReference<List<Window>>() {
				});
		listWindow = response.getBody();
		return listWindow;
	}

	
	/**
	 * 
	 * @return list of people sensors
	 */
	@GetMapping(path = "/listNbrPeopleSensor", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<NbrPeopleSensor> getListNbrPeopleSensor() {
		List<NbrPeopleSensor> listSensor;

		ResponseEntity<List<NbrPeopleSensor>> response = restTemplate.exchange(
				"http://NbrPeopleSensorsService/nbrPeopleSensor/list", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<NbrPeopleSensor>>() {
				});
		listSensor = response.getBody();
		return listSensor;
	}

	/**
	 * 
	 * @param value
	 * @param room
	 */
	@PostMapping(path = "/addTemperatureValueRoom/{room}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addTemperatureValue(@RequestBody SensorValue value, @PathVariable int room) {
		// RestTemplate restTemplate=new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<SensorValue> httpEntity = new HttpEntity<SensorValue>(value, headers);
		restTemplate.postForObject("http://TemperatureSensorsService/temperature/addValueRoom/" + String.valueOf(room),
				httpEntity, Object.class);

	}

	/**
	 * 
	 * @param nbrPeopleThreshold
	 * @param outTempThreshold
	 * @param deltaTempThreshold
	 * @param autoManagement
	 */
	@PutMapping(path = "/setRules/{nbrPeopleThreshold}/{outTempThreshold}/{deltaTempThreshold}/{autoManagement}")
	public void setManagementRules(@PathVariable int nbrPeopleThreshold, @PathVariable double outTempThreshold,
			@PathVariable double deltaTempThreshold, @PathVariable boolean autoManagement) {

		this.deltaTempThredhold = deltaTempThreshold;
		this.nbrPeopleThreshold = nbrPeopleThreshold;
		this.outdoorTempThreshold = outTempThreshold;
		this.autoManagement = autoManagement;

		if (this.autoManagement) {
			windowManager(this.outdoorTempThreshold, this.deltaTempThredhold, this.nbrPeopleThreshold);
		}

	}

	/**
	 * Create a subscription on OM2M at the CSE level to be notified as soon as a new resource is created. When a new resource
	 * is created the function subscribetoRssource is called
	 * 
	 */
	@PostMapping(path = "/initOM2M")
	public void initOM2M() {

		RestTemplate restTemplateLocal = new RestTemplate();
		String const_nu = "http://localhost:8083/";
		String nu = "subscribeToRessource";

		JSONObject sub = new JSONObject();
		sub.put("rn", "Sub");
		sub.put("nu", const_nu + nu);
		sub.put("nct", 2);

		JSONObject body = new JSONObject();
		body.put("m2m:sub", sub);

		ResponseEntity<String> response;

		HttpHeaders headers = new HttpHeaders();

		headers.set("X-M2M-Origin", "admin:admin");
		headers.set("Content-Type", "application/json;ty=23");
		headers.set("accept", "application/json");

		HttpEntity<String> httpEntity = new HttpEntity<String>(body.toString(), headers);

		response = restTemplateLocal.exchange("http://localhost:8082/~/in-cse/in-name/", HttpMethod.POST, httpEntity,
				String.class);
	}

	/**
	 * Called as soon as a new resource is created on OM2M. Subscribe to the newly resource for retrieve new data. 
	 * @param request, a notification response
	 */
	@PostMapping(path = "/subscribeToRessource", consumes = MediaType.ALL_VALUE)
	public void subscribeToRessource(@RequestBody String request) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		JSONObject json_request = new JSONObject(request);
		JSONObject con = json_request.getJSONObject("m2m:sgn");

		//The verification package is ignored
		if (!(con.has("m2m:vrq"))) {

			JSONObject con_bis = con.getJSONObject("m2m:nev").getJSONObject("m2m:rep").getJSONObject("m2m:ae");

			//Retrieve label of the created resource
			JSONObject lbl = new JSONObject(con_bis.getJSONArray("lbl").getString(0));
			String ressource_type = lbl.getString("RessourceType");

			int id = 0;
			int room = 0;

			String const_nu = "http://localhost:8083/";
			String nu = null;

			//According to resource type the orchestrator subscribes to the resource with diferent nu values. 
			switch (ressource_type) {

			case "People":
				id = lbl.getInt("ID");
				room = lbl.getInt("room");

				NbrPeopleSensor peopleSensor = new NbrPeopleSensor(id, room);
				restTemplate.postForObject("http://NbrPeopleSensorsService/nbrPeopleSensor/addSensor/",
						new HttpEntity<NbrPeopleSensor>(peopleSensor, headers), String.class);
				nu = const_nu + "receivePeopleData";

				break;
			case "Temperature":

				id = lbl.getInt("ID");
				room = lbl.getInt("room");

				TemperatureSensor sensor = new TemperatureSensor(id, room);
				restTemplate.postForObject("http://TemperatureSensorsService/temperature/addSensor/",
						new HttpEntity<TemperatureSensor>(sensor, headers), String.class);
				nu = const_nu + "receiveTemperatureData";

				break;
			case "Window":

				id = lbl.getInt("ID");
				room = lbl.getInt("room");

				Window window = new Window(id, room);
				restTemplate.postForObject("http://WindowManagementService/window/addWindow/",
						new HttpEntity<Window>(window, headers), String.class);

				nu = const_nu + "receiveWindowData";
				break;
			}

			if (!(nu == null)) {

				try {
					Thread.sleep(500); //Wait until the container DATA is created under the new resource
				} catch (Exception e) {
					e.printStackTrace();
				}

				RestTemplate restTemplateLocal = new RestTemplate();

				JSONObject sub = new JSONObject();
				sub.put("rn", "Sub");
				sub.put("nu", nu);
				sub.put("nct", 2);

				JSONObject body = new JSONObject();
				body.put("m2m:sub", sub);

				ResponseEntity<String> response;

				headers.set("X-M2M-Origin", "admin:admin");
				headers.set("Content-Type", "application/json;ty=23");
				headers.set("accept", "application/json");

				HttpEntity<String> httpEntity = new HttpEntity<String>(body.toString(), headers);

				
				//Create in the DATA container
				response = restTemplateLocal.exchange(
						"http://localhost:8082/~/in-cse/in-name/" + con_bis.getString("rn") + "/DATA", HttpMethod.POST,
						httpEntity, String.class);

				JSONObject idRoom = new JSONObject();
				idRoom.put("id", id);
				idRoom.put("room", room);

				if (ressource_type.equals("Window")) {
					
					//register uri according to the id and room of the actuator
					uriWindows.put(idRoom.toString(),
							"http://localhost:8082/~/in-cse/in-name/" + con_bis.getString("rn") + "/DATA");

				}

				//register uri of the subscription 
				subRessources.put(new JSONObject(response.getBody()).getJSONObject("m2m:sub").getString("ri"), idRoom);

			}

		}

	}

	/**
	 * Is called as soon as a new temperature data is posted. The new data is stored in the Temperature MS database. If automanagement activated, window actuators
	 * are managed
	 * @param request
	 */
	@PostMapping(path = "/receiveTemperatureData", consumes = MediaType.ALL_VALUE)
	public void receiveTemperatureData(@RequestBody String request) {

		JSONObject json_request = new JSONObject(request);
		JSONObject con = json_request.getJSONObject("m2m:sgn");

		if (!(con.has("m2m:vrq"))) {

			String subRI = con.getString("m2m:sur");

			int id = subRessources.get(subRI).getInt("id");
			int room = subRessources.get(subRI).getInt("room");

			JSONObject con_bis = new JSONObject(
					con.getJSONObject("m2m:nev").getJSONObject("m2m:rep").getJSONObject("m2m:cin").getString("con"));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			if (!restTemplate.getForObject("http://TemperatureSensorsService/temperature/isMeasured/"
					+ String.valueOf(id) + "/" + String.valueOf(con_bis.getLong("timestamp")), boolean.class)) {
				restTemplate
						.postForObject("http://TemperatureSensorsService/temperature/addValueID/" + String.valueOf(id),
								new HttpEntity<SensorValue>(new SensorValue(con_bis.getDouble("value"),
										con_bis.getLong("timestamp"), con_bis.getString("unit")), headers),
								String.class);

				//if automatic window management
				if (autoManagement) {

					//room -1 corresponds to outdoor environment. Room for outdoor temperature sensor. We check all window actuator
					if (room != -1) {
						windowManagerRoom(outdoorTempThreshold, deltaTempThredhold, nbrPeopleThreshold, room);
					} else {
						//We check only the specified room window actuator
						windowManager(outdoorTempThreshold, deltaTempThredhold, nbrPeopleThreshold);
					}
				}
			}

		}

	}

	/**
	 * Idem that for temperature data
	 * @param request
	 */
	@PostMapping(path = "/receivePeopleData", consumes = MediaType.ALL_VALUE)
	public void receivePeopleData(@RequestBody String request) {

		JSONObject json_request = new JSONObject(request);
		JSONObject con = json_request.getJSONObject("m2m:sgn");

		if (!(con.has("m2m:vrq"))) {

			String subRI = con.getString("m2m:sur");
			int id = subRessources.get(subRI).getInt("id");
			int room = subRessources.get(subRI).getInt("room");

			JSONObject con_bis = new JSONObject(
					con.getJSONObject("m2m:nev").getJSONObject("m2m:rep").getJSONObject("m2m:cin").getString("con"));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			if (!restTemplate.getForObject("http://NbrPeopleSensorsService/nbrPeopleSensor/isMeasured/"
					+ String.valueOf(id) + "/" + String.valueOf(con_bis.getLong("timestamp")), boolean.class)) {
				restTemplate.postForObject(
						"http://NbrPeopleSensorsService/nbrPeopleSensor/addValue/" + String.valueOf(id),
						new HttpEntity<NbrPeopleValue>(
								new NbrPeopleValue(con_bis.getInt("nbrPeople"), con_bis.getLong("timestamp")), headers),
						String.class);
				
				if(autoManagement) {
				windowManagerRoom(outdoorTempThreshold, deltaTempThredhold, nbrPeopleThreshold, room);
				}
			}
		}

	}

	/**
	 * Idem that for temperature data
	 * @param request
	 */
	@PostMapping(path = "/receiveWindowData", consumes = MediaType.ALL_VALUE)
	public void receiveWindowData(@RequestBody String request) {

		JSONObject json_request = new JSONObject(request);
		JSONObject con = json_request.getJSONObject("m2m:sgn");

		if (!(con.has("m2m:vrq"))) {

			String subRI = con.getString("m2m:sur");
			int id = subRessources.get(subRI).getInt("id");

			JSONObject con_bis = new JSONObject(
					con.getJSONObject("m2m:nev").getJSONObject("m2m:rep").getJSONObject("m2m:cin").getString("con"));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			if (!restTemplate.getForObject("http://WindowManagementService/window/isMeasured/" + String.valueOf(id)
					+ "/" + String.valueOf(con_bis.getLong("timestamp")), boolean.class)) {

				restTemplate.postForObject("http://WindowManagementService/window/addStateID/" + String.valueOf(id),
						new HttpEntity<WindowState>(
								new WindowState(con_bis.getBoolean("state"), con_bis.getLong("timestamp")), headers),
						String.class);
			}

		}

	}

	/**
	 * Checks according to the threshold values passed in parameters the actions to give to the windows. The actions are posted on OM2M. He all room are checked
	 * @param outdoor_threshold
	 * @param delta_temp_threshold
	 * @param threshold_nbrPeople
	 */
	@PostMapping(path = "/ManageWindow/{outdoor_threshold}/{delta_temp_threshold}/{threshold_nbrPeople}")
	public void windowManager(@PathVariable double outdoor_threshold, @PathVariable double delta_temp_threshold,
			@PathVariable int threshold_nbrPeople) {

		RestTemplate restTemplateLocal = new RestTemplate();

		List<Window> listWindow = getListWindow();
		List<TemperatureSensor> listTempSensor = getListTemperatureSensor();
		;
		List<NbrPeopleSensor> listNbrPeopleSensor;

		listNbrPeopleSensor = getListNbrPeopleSensor();

		List<SensorValue> valuesOutdoor = restTemplate
				.getForObject("http://TemperatureSensorsService/temperature/room/-1", TemperatureSensor.class)
				.getValues();

		for (int i = 0; i < listWindow.size(); i++) {

			boolean windowState = false;

			double outdoorTemp = valuesOutdoor.get(valuesOutdoor.size() - 1).getValue();
			int room = listWindow.get(i).getRoom();
			int id = listWindow.get(i).getId();
			System.out.println(outdoorTemp);

			if (room != -1) {

				List<SensorValue> valuesIndoor = restTemplate
						.getForObject("http://TemperatureSensorsService/temperature/room/" + String.valueOf(room),
								TemperatureSensor.class)
						.getValues();

				if (valuesIndoor.size() > 0) {
					double indoorTemp = valuesIndoor.get(valuesIndoor.size() - 1).getValue();
					int nbrPeople = 0;

					List<NbrPeopleValue> peopleSensorValues = restTemplate
							.getForObject("http://NbrPeopleSensorsService/nbrPeopleSensor/room/" + String.valueOf(room),
									NbrPeopleSensor.class)
							.getValues();

					if (peopleSensorValues.size() > 0) {
						nbrPeople = peopleSensorValues.get(peopleSensorValues.size() - 1).getNbrPeople();
					}

					double delta_temp = Math.abs(outdoorTemp - indoorTemp);

					System.out.println(delta_temp);
					if ((outdoorTemp > outdoor_threshold && delta_temp < delta_temp_threshold)
							|| (nbrPeople <= threshold_nbrPeople)) {
						windowState = true;

					}

					String con = "{\"state\":" + windowState + "," + "\"timestamp\":" + System.currentTimeMillis()
							+ "}";

					postActionOM2M("Window", id, room, con);
				}

			}
		}

	}

	/**
	 * As the previous function but only for one room
	 * @param outdoor_threshold
	 * @param delta_temp_threshold
	 * @param threshold_nbrPeople
	 * @param room
	 */
	@PostMapping(path = "/ManageWindowRoom/{room}/{outdoor_threshold}/{delta_temp_threshold}/{threshold_nbrPeople}")
	public void windowManagerRoom(@PathVariable double outdoor_threshold, @PathVariable double delta_temp_threshold,
			@PathVariable int threshold_nbrPeople, @PathVariable int room) {

		RestTemplate restTemplateLocal = new RestTemplate();

		List<SensorValue> valuesOutdoor = restTemplate
				.getForObject("http://TemperatureSensorsService/temperature/room/-1", TemperatureSensor.class)
				.getValues();
		double outdoorTemp = valuesOutdoor.get(valuesOutdoor.size() - 1).getValue();

		TemperatureSensor tSensor = restTemplate.getForObject(
				"http://TemperatureSensorsService/temperature/room/" + String.valueOf(room), TemperatureSensor.class);
		Window window = restTemplate.getForObject("http://WindowManagementService/window/room/" + String.valueOf(room),
				Window.class);
		NbrPeopleSensor peopleSensor = restTemplate.getForObject(
				"http://NbrPeopleSensorsService/nbrPeopleSensor/room/" + String.valueOf(room), NbrPeopleSensor.class);

		boolean windowState = false;

		if (room != -1) {
			List<SensorValue> valuesIndoor = tSensor.getValues();
			List<NbrPeopleValue> nbrPeopleValues = peopleSensor.getValues();
			int nbrPeople = 0;

			double indoorTemp = valuesIndoor.get(valuesIndoor.size() - 1).getValue();

			if (nbrPeopleValues.size() > 0) {
				nbrPeople = nbrPeopleValues.get(nbrPeopleValues.size() - 1).getNbrPeople();
			}

			double delta_temp = Math.abs(outdoorTemp - indoorTemp);

			if ((outdoorTemp > outdoor_threshold && delta_temp < delta_temp_threshold)
					|| (nbrPeople <= threshold_nbrPeople)) {
				windowState = true;
			}

			String con = "{\"state\":" + windowState + "," + "\"timestamp\":" + System.currentTimeMillis() + "}";

			postActionOM2M("Window", window.getId(), room, con);

		}

	}
	
	/**
	 * add a new state to a window actuator
	 * @param room
	 * @param state
	 */
	@PutMapping(path="/changeWindowState/{room}/{state}")
	public void changeStateWindow(@PathVariable int room,@PathVariable boolean state) {
		Window window = restTemplate.getForObject("http://WindowManagementService/window/room/" + String.valueOf(room),
				Window.class);
		
		String con = "{\"state\":" + state + "," + "\"timestamp\":" + System.currentTimeMillis() + "}";
		postActionOM2M("Window", window.getId(), room, con);

	}

	/**
	 * Post a actuator action to OM2M. For instance open or close for a window
	 * @param actuatorType
	 * @param targetID
	 * @param targetRoom
	 * @param con
	 */
	public void postActionOM2M(String actuatorType, int targetID, int targetRoom, String con) {

		ResponseEntity<String> response;
		HttpHeaders headers = new HttpHeaders();

		headers.set("X-M2M-Origin", "admin:admin");
		headers.set("Content-Type", "application/json;ty=4");
		headers.set("accept", "application/json");

		HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

		RestTemplate restTemplateLocal = new RestTemplate();

		JSONObject idRoom = new JSONObject();
		idRoom.put("id", targetID);
		idRoom.put("room", targetRoom);
		System.out.println(idRoom.toString());

		System.out.println(uriWindows.get(idRoom.toString()));
		String uriWindow = uriWindows.get(idRoom.toString());

		JSONObject body = new JSONObject();
		JSONObject conJSON = new JSONObject();

		conJSON.put("con", con);

		body.put("m2m:cin", conJSON);

		System.out.println(body);
		System.out.println(uriWindows.toString());

		restTemplateLocal.exchange("" + uriWindow, HttpMethod.POST, new HttpEntity<String>(body.toString(), headers),
				Object.class);

	}

}
