package de.quantumrange.verbo.model;

import lombok.*;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor

@Table(appliesTo = "view")
@Entity(name = "view")
public final class WordView {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, updatable = false)
	private long id;

	@ManyToOne
	@JoinColumn(name = "word_id", nullable = false)
	private Word word;

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
	
	@Column(nullable = false)
	private LocalDateTime timestamp;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String answer;
	
	@Column(nullable = false)
	private int correctness;
	
	@Enumerated(EnumType.ORDINAL)
	private AnswerClassification classification;
	
	@Column(nullable = false)
	private int answerDuration;
	
	@Column(nullable = false)
	private LearningMode mode;
	
	@Column(nullable = false)
	private boolean reversed;

}