package de.quantumrange.verbo.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Table;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

@Table(appliesTo = "word")
@Entity(name = "word")
public class Word implements Identifiable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, updatable = false)
	private long id;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private Vocabulary owner;
	
	@Column(nullable = false)
	private String question;
	@Enumerated(EnumType.STRING)
	private Language questionLang;
	@Column(nullable = false)
	private String answer;
	@Enumerated(EnumType.STRING)
	private Language answerLang;
	
}