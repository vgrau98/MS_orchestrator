package com.insa.projet.microservices.orchestrator_MS.model.nbrPeople;

public class NbrPeopleValue {

	private int nbrPeople;
	private long timestamp;
	
	public NbrPeopleValue(int nbrPeople, long timestamp) {
		this.nbrPeople = nbrPeople;
		this.timestamp = timestamp;
	}

	public int getNbrPeople() {
		return nbrPeople;
	}

	public void setNbrPeople(int nbrPeople) {
		this.nbrPeople = nbrPeople;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
