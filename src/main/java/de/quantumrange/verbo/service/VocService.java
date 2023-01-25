package de.quantumrange.verbo.service;

import de.quantumrange.verbo.model.Word;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

@Service
public class VocService implements DataService<Word> {
	
	private static final File file = new File("voc.json");
	
	private final @NotNull List<Word> word;
	private final @NotNull Map<Long, Word> linker;
	private final @NotNull Set<Long> ids;
	
	public VocService() {
		this.word = _read(file)
				.orElse(new ArrayList<>());
		this.linker = new HashMap<>();
		this.ids = new HashSet<>();
		
		for (Word v : word) {
			this.linker.put(v.getId(), v);
			this.ids.add(v.getId());
			
			
		}
		
		save();
	}
	
	@Override
	public @NotNull Optional<Word> findByID(long id) {
		if (!ids.contains(id)) return Optional.empty();
		
		return Optional.of(this.linker.get(id));
	}
	
	@Override
	public void update(@NotNull Word data) {
		word.replaceAll(v -> {
			if (v.getId() == data.getId()) return data;
			else return v;
		});
		
		linker.replace(data.getId(), data);
		
		save();
	}
	
	@Override
	public void insert(@NotNull Word data) {
		word.add(data);
		linker.put(data.getId(), data);
		ids.add(data.getId());
		
		save();
	}
	
	@Override
	public void remove(@NotNull Word data) {
		ids.remove(data.getId());
		linker.remove(data.getId());
		word.remove(data);
		
		save();
	}
	
	@Override
	public boolean exist(long id) {
		return ids.contains(id);
	}
	
	@Override
	public Stream<Word> stream() {
		return word.stream();
	}
	
	@Override
	public Stream<Word> parallel() {
		return word.parallelStream();
	}
	
	@Override
	public void save() {
		_write(file, word);
	}
	
	@Override
	public Class<Word> getClazz() {
		return Word.class;
	}
}
