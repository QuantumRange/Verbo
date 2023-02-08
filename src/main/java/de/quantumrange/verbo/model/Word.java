package de.quantumrange.verbo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Table(appliesTo = "word")
@Entity(name = "word")
public class Word implements Identifiable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, updatable = false)
	private long id;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private WordSet owner;
	
	@Column(nullable = false)
	private String question;
	@Column(nullable = false)
	private String answer;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Word word = (Word) o;
		return getId() == word.getId();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
	
	@Override
	public String toString() {
		return "Word{" +
				"id=" + id +
				", owner=" + owner.getId() +
				", question='" + question + '\'' +
				", answer='" + answer + '\'' +
				'}';
	}
}
