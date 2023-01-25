package de.quantumrange.verbo.model;

import lombok.*;
import org.hibernate.annotations.Table;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

@Deprecated(forRemoval = true)
public class WordInfo {
	
	private long snowflake;
	private int correct, wrong;
	private @NotNull Set<WordView> views;
	private boolean starred;
	private @NotNull String note;
	
}