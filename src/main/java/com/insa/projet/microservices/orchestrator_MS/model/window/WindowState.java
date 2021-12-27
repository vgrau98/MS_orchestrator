package com.insa.projet.microservices.orchestrator_MS.model.window;

public class WindowState {
	private boolean state;
	private long timestamp;
	
	public WindowState(boolean state, long timestamp) {
		super();
		this.state = state;
		this.timestamp = timestamp;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	

}
