package de.quantumrange.verbo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(appliesTo = "course")
@Entity(name = "course")
public class Course implements Identifiable {
	
	@Id
	@GenericGenerator(name = "id",
			strategy = "de.quantumrange.verbo.model.generator.IdGenerator",
			parameters = {@Parameter(name = "table", value = "course")})
	@GeneratedValue(generator = "id")
	private long id;
	@Column(nullable = false)
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "owner_id")
	private User owner;
	
	@ManyToMany
	@JoinTable(name = "course_user",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "user_id"))
	private Set<User> users;

	@ManyToMany
	@JoinTable(name = "course_vocabularies",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "set_id"))
	private Set<WordSet> wordSets;
	
	// For tests, etc.
	@Column(nullable = false)
	private String currentNote;
	
	@ManyToMany
	@JoinTable(name = "course_current_set",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "set_id"))
	private Set<WordSet> wordSetInTest;
	
	@Column(nullable = false)
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
		return Objects.hash(getId(), getName());
	}
}
