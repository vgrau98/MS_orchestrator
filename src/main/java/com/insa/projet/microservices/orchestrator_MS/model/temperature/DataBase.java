package com.insa.projet.microservices.orchestrator_MS.model.temperature;

import java.util.ArrayList;
import java.util.List;

public class DataBase {
	
	private List<TemperatureSensor> listSensors;

	public DataBase() {
		listSensors=new ArrayList<TemperatureSensor>();
			}

	public List<TemperatureSensor> getListSensors() {
		return listSensors;
	}

	public void setListSensors(List<TemperatureSensor> listSensors) {
		this.listSensors = listSensors;
	}
	
	//n number of room. One sensor per room + one outdoor
	public void initDataBase(int n){
		
		this.listSensors.clear();
		
		//Outdoor sensor
		this.listSensors.add(new TemperatureSensor(0, -1));
	
		for(int i=0;i<n;i++) {
			this.listSensors.add(new TemperatureSensor(i+1, i+100));
		}

		
	}
	
}
