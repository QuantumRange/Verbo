package de.quantumrange.verbo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Table(appliesTo = "live_session")
@Entity(name = "live_session")
public class LiveSession {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, updatable = false)
	private long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User owner;
	
	@ManyToOne
	@JoinColumn(name = "set_id", nullable = false)
	private WordSet set;
	
	@ManyToMany
	@JoinTable(name = "user_marked",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "marked_id"))
	private Set<User> participants;
	
}
