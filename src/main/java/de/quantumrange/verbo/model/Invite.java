package de.quantumrange.verbo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Random;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Table(appliesTo = "invite")
@Entity(name = "invite")
public class Invite {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, updatable = false)
	private long id;
	
	@Column(nullable = false)
	private String code;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
	
	@Column(nullable = false)
	private LocalDateTime created;
	
	@Column(nullable = false)
	private boolean valid;
	
	public static String generateCode() {
		char[] allowed = "0123456789ABDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < 9; i++) {
			if (i == 4) builder.append('-');
			else builder.append(allowed[new Random().nextInt(allowed.length)]);
		}
		
		return builder.toString();
	}
	
}
