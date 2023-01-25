package de.quantumrange.verbo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VocSet implements Identifiable {
	
	public static final DateTimeFormatter SAVE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	private static final DateTimeFormatter GERMAN_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
	private static final DateTimeFormatter SIMPLE_GERMAN_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
	private long id;
	private String name;
	private long owner;
	private String timestamp;
	private Set<Long> vocabularies;
	
	@JsonIgnore
	public String getOwnerStr() {
		return Identifiable.getVisibleId(owner);
	}
	
	@JsonIgnore
	public String getTimestampStr() {
		return SIMPLE_GERMAN_FORMAT.format(SAVE_FORMAT.parse(timestamp));
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VocSet vocSet = (VocSet) o;
		return getId() == vocSet.getId();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
