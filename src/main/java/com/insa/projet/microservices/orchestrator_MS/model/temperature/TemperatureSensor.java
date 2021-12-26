package com.insa.projet.microservices.orchestrator_MS.model.temperature;

import java.util.ArrayList;
import java.util.List;

public class TemperatureSensor {

	private int id;
	private int room;
	private List<SensorValue> values;

	public TemperatureSensor() {

	}

	public TemperatureSensor(int id, int room) {
		this.id = id;
		this.room = room;
		values = new ArrayList<SensorValue>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRoom() {
		return room;
	}

	public void setRoom(int room) {
		this.room = room;
	}

	public List<SensorValue> getValues() {
		return values;
	}

	public void addValue(SensorValue value) {
		this.values.add(value);
	}

}
