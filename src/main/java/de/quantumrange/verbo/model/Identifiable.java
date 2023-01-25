package de.quantumrange.verbo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public interface Identifiable {
	
	static String getVisibleId(long id) {
		return Long.toString(id, 35).toUpperCase();
	}
	
	static long getId(String visibleID) {
		return Long.parseLong(visibleID, 35);
	}
	
	static long generateId(Stream<? extends Identifiable> identifiable) {
		AtomicLong id = new AtomicLong();
		
		do {
			id.set(new Random().nextLong());
		} while (identifiable.anyMatch(i -> i.getId() == id.get()));
		
		return id.get();
	}
	
	long getId();
	
	@JsonIgnore
	default String getVisibleId() {
		return getVisibleId(getId());
	}
	
}
