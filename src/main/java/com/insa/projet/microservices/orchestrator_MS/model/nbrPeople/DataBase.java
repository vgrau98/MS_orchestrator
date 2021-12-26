package com.insa.projet.microservices.orchestrator_MS.model.nbrPeople;

import java.util.ArrayList;
import java.util.List;

public class DataBase {
	
	private List<NbrPeopleSensor> listSensors;

	public DataBase() {
		listSensors=new ArrayList<NbrPeopleSensor>();
			}

	public List<NbrPeopleSensor> getListSensors() {
		return listSensors;
	}

	public void setListSensors(List<NbrPeopleSensor> listSensors) {
		this.listSensors = listSensors;
	}
	
	public void initDataBase(int n){
		
		this.listSensors.clear();
		for(int i=0;i<n;i++) {
			this.listSensors.add(new NbrPeopleSensor(i, i+100));
		}
		
	}

}
