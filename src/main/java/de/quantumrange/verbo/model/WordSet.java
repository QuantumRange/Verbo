package de.quantumrange.verbo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

@Table(appliesTo = "word_set")
@Entity(name = "word_set")
public class WordSet implements Identifiable {
	
	// TODO: Config with custom format type
	
	@Deprecated(forRemoval = true)
	public static final DateTimeFormatter SAVE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	
	@Deprecated(forRemoval = true)
	private static final DateTimeFormatter GERMAN_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
	
	@Deprecated(forRemoval = true)
	private static final DateTimeFormatter SIMPLE_GERMAN_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
	
	@Id
	@GenericGenerator(name = "id",
			strategy = "de.quantumrange.verbo.model.generator.IdGenerator",
			parameters = {@org.hibernate.annotations.Parameter(name = "table", value = "word_set")})
	@GeneratedValue(generator = "id")
	private long id;
	
	@Column(nullable = false)
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
	
	@ManyToMany
	@JoinTable(name = "editor_word_set",
			joinColumns = @JoinColumn(name = "set_id"),
			inverseJoinColumns = @JoinColumn(name = "user_id"))
	private Set<User> editors;
	
	@ManyToMany
	@JoinTable(name = "user_marked", joinColumns = @JoinColumn(name = "marked_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	private Set<User> markedBy;
	
	@Enumerated(EnumType.STRING)
	private Language question;
	
	@Enumerated(EnumType.STRING)
	private Language answer;
	
	@Enumerated(EnumType.ORDINAL)
	private EditPolicy editPolicy;
	
	@Column(nullable = false)
	private LocalDateTime timestamp;
	
	@OneToMany(mappedBy = "owner")
	private Set<Word> words;
	
	
	public boolean canEdit(User user) {
		return user.getRole() == Role.ADMIN || editors.contains(user) || owner.equals(user);
	}
	
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
	
	@Override
	public String toString() {
		return "WordSet{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
