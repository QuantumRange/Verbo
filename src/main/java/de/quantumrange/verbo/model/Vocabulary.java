package de.quantumrange.verbo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

@Table(appliesTo = "vocabulary")
@Entity(name = "vocabulary")
public class Vocabulary implements Identifiable {
	
	// TODO: Config with custom format type
	public static final DateTimeFormatter SAVE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	private static final DateTimeFormatter GERMAN_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
	private static final DateTimeFormatter SIMPLE_GERMAN_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	@Id
	@GenericGenerator(name = "id",
			strategy = "de.quantumrange.verbo.model.generator.IdGenerator",
			parameters = {@org.hibernate.annotations.Parameter(name = "table", value = "vocabulary")})
	@GeneratedValue(generator = "id")
	private long id;
	
	@Column(nullable = false)
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
	
	@Column(nullable = false)
	private LocalDateTime timestamp;
	
	@OneToMany(mappedBy = "owner_id")
	private Set<Word> words;
	
	
	// Thymeleaf usages
	@Deprecated(forRemoval = true)
	@JsonIgnore
	public String getOwnerStr() {
		return Identifiable.getVisibleId(owner.getId());
	}
	
	@Deprecated(forRemoval = true)
	@JsonIgnore
	public String getTimestampStr() {
		return SIMPLE_GERMAN_FORMAT.format(timestamp);
	}
	
}
