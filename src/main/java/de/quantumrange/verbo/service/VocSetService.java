package de.quantumrange.verbo.service;

import de.quantumrange.verbo.model.Vocabulary;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

@Service
public class VocSetService implements DataService<Vocabulary> {
	
	private static final File file = new File("sets.json");
	
	private final @NotNull List<Vocabulary> sets;
	private final @NotNull Map<Long, Vocabulary> linker;
	
	public VocSetService() {
		this.sets = _read(file)
				.orElse(new ArrayList<>());
		linker = new HashMap<>();
		
		for (Vocabulary set : sets) {
			linker.put(set.getId(), set);
		}
		
		save();
	}
	
	@Override
	public @NotNull Optional<Vocabulary> findByID(long id) {
		return Optional.ofNullable(linker.getOrDefault(id, null));
	}
	
	@Override
	public void update(@NotNull Vocabulary data) {
		sets.replaceAll(vocSet -> {
			if (vocSet.getId() == data.getId()) {
				return data;
			} else return vocSet;
		});
		linker.replace(data.getId(), data);
		
		save();
	}
	
	@Override
	public void insert(@NotNull Vocabulary data) {
		sets.add(data);
		linker.put(data.getId(), data);
		
		save();
	}
	
	@Override
	public void remove(@NotNull Vocabulary data) {
		sets.remove(data);
		linker.remove(data.getId());
		
		save();
	}
	
	@Override
	public boolean exist(long id) {
		return linker.containsKey(id);
	}
	
	@Override
	public Stream<Vocabulary> stream() {
		return sets.stream();
	}
	
	@Override
	public Stream<Vocabulary> parallel() {
		return sets.parallelStream();
	}
	
	@Override
	public void save() {
		_write(file, sets);
	}
	
	@Override
	public Class<Vocabulary> getClazz() {
		return Vocabulary.class;
	}
}
