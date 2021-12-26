package com.insa.projet.microservices.orchestrator_MS.model.window;

public class Window {

	private int id;
	private int room;
	private boolean state;

	public Window() {
		
	}

	public Window(int id, int room, boolean state) {
		this.id = id;
		this.room = room;
		this.state = state;
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

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

}
