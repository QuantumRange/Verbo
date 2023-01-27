package de.quantumrange.verbo.model;

import lombok.*;
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
	@Enumerated(EnumType.STRING)
	private Language questionLang;
	@Column(nullable = false)
	private String answer;
	@Enumerated(EnumType.STRING)
	private Language answerLang;
	
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
}
