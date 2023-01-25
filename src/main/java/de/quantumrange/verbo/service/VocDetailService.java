package de.quantumrange.verbo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.quantumrange.verbo.model.WordInfo;
import de.quantumrange.verbo.model.WordView;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Service
public class VocDetailService {
	
	private static final File folder = new File("./detail/");
	
	private final Map<Long, Map<Long, WordInfo>> data;
	
	public VocDetailService() {
		this.data = new HashMap<>();
		folder.mkdirs();
		
		JsonMapper mapper = new JsonMapper();
		
		for (File file : folder.listFiles()) {
			try {
				long id = Long.parseLong(file.getName().replaceAll("[^\\d]", ""));
				Map<String, WordInfo> temp = mapper.readerForMapOf(WordInfo.class)
						.readValue(new FileInputStream(file));
				
				this.data.put(id, new HashMap<>());
				
				for (String key : temp.keySet()) {
					long l = Long.parseLong(key);
					
					this.data.get(id).put(l, temp.get(key));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public @NotNull Optional<WordInfo> findViewBy(long userID, long voc) {
		return findViewBy(userID)
				.map(map -> map.getOrDefault(voc, null));
	}
	
	public @NotNull Optional<Map<Long, WordInfo>> findViewBy(long userID) {
		return Optional.ofNullable(data.getOrDefault(userID, null));
	}
	
	public void update(long userID,
	                   @NotNull WordInfo info) {
		if (!data.containsKey(userID)) {
			data.put(userID, new HashMap<>());
		}
		
		data.get(userID).put(info.getSnowflake(), info);
		
		save(userID);
	}
	
	public void insert(long userID,
	                   @NotNull WordInfo info) {
		if (!data.containsKey(userID)) {
			data.put(userID, new HashMap<>());
		}
		
		data.get(userID).put(info.getSnowflake(), info);
		
		save(userID);
	}
	
	public void insert(long userID,
	                   long vocID,
	                   @NotNull WordView view) {
		if (!data.containsKey(userID)) {
			data.put(userID, new HashMap<>());
		}
		
		if (!data.get(userID).containsKey(vocID)) {
			data.get(userID).put(vocID, new WordInfo(vocID,
					0,
					0,
					new HashSet<>(),
					false,
					""));
		}
		
		data.get(userID).get(vocID).getViews().add(view);
		
		save(userID);
	}
	
	public Map<Long, Map<Long, WordInfo>> getData() {
		return data;
	}
	
	public void save(long userID) {
		JsonMapper mapper = new JsonMapper();
		
		try {
			mapper.writerFor(new TypeReference<Map<Long, WordInfo>>() {
					})
					.writeValue(new File(folder, userID + ".json"), data.get(userID));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
