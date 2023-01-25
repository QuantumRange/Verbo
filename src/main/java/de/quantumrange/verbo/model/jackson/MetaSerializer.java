package de.quantumrange.verbo.model.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.quantumrange.verbo.model.MetaData;
import de.quantumrange.verbo.model.MetaKey;

import java.io.IOException;

public class MetaSerializer extends StdSerializer<MetaData> {
	
	protected MetaSerializer() {
		super(MetaData.class);
	}
	
	@Override
	public void serialize(MetaData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartArray();
		
		for (MetaKey<?> key : value.keySet()) {
			Object val = value.get(key);
			
			gen.writeStartObject();
			
			gen.writeStringField("name", key.getName());
			gen.writeObjectField("obj", val);
			
			gen.writeEndObject();
		}
		
		gen.writeEndArray();
	}
	
}
