package de.quantumrange.verbo.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.quantumrange.verbo.model.jackson.MetaDeserializer;
import de.quantumrange.verbo.model.jackson.MetaSerializer;

import java.util.HashMap;

@JsonSerialize(using = MetaSerializer.class)
@JsonDeserialize(using = MetaDeserializer.class)
public class MetaData extends HashMap<MetaKey<?>, Object> {
	
	public MetaData() {
		super(MetaKey.values.length);
	}
	
}
