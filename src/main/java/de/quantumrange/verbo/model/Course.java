package de.quantumrange.verbo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Course implements Identifiable {
	
	private long id;
	private String name;
	private Set<Long> users;
	private Set<Long> sets;
	
	// For tests, etc.
	private String currentNote;
	private Set<Long> currentSets;
	private String code;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Course course = (Course) o;
		return getId() == course.getId();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
	
	@Override
	public long getId() {
		return id;
	}
}
