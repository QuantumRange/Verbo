package de.quantumrange.verbo.model.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.quantumrange.verbo.model.MetaData;
import de.quantumrange.verbo.model.MetaKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MetaDeserializer extends StdDeserializer<MetaData> {
	
	private static final Logger log = LoggerFactory.getLogger(MetaDeserializer.class);
	
	public MetaDeserializer() {
		super(MetaData.class);
	}
	
	@Override
	public MetaData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		MetaData metadata = new MetaData();
		
		TreeNode node = p.getCodec().readTree(p);
		
		for (int i = 0; i < node.size(); i++) {
			JsonNode treeNode = (JsonNode) node.get(i);
			
			String name = treeNode.get("name").asText();
			MetaKey<?> key = null;
			
			for (MetaKey<?> value : MetaKey.values) {
				if (value.getName().equals(name)) {
					key = value;
				}
			}
			
			if (key != null) {
				ObjectMapper mapper = new JsonMapper();
				try {
					metadata.put(key, mapper.readerFor(key.getClazz())
							.readValue(treeNode.get("obj")));
				} catch (Exception e) {
					log.warn("Can't resolve Metakey {} with input json '{}'.", key.getName(), treeNode.get("obj").asText());
				}
			} else log.warn("Can't resolve MetaKey with string {}.", name);
		}
		
		return metadata;
	}
	
}
