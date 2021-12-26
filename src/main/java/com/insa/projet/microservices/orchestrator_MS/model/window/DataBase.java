package com.insa.projet.microservices.orchestrator_MS.model.window;

import java.util.ArrayList;
import java.util.List;



public class DataBase {

	private List<Window> listWindows;

	public DataBase() {
		listWindows = new ArrayList<Window>();
	}

	public List<Window> getListWindows() {
		return listWindows;
	}

	public void setListWindows(List<Window> listWindows) {
		this.listWindows = listWindows;
	}
	
	public void initDataBase(int n) {
		this.listWindows.clear();
		for(int i=0;i<n;i++) {
			this.listWindows.add(new Window(i, i+100, false));
		}
	}
}
