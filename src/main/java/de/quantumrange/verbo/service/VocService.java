package de.quantumrange.verbo.service;

import de.quantumrange.verbo.model.Voc;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

@Service
public class VocService implements DataService<Voc> {
	
	private static final File file = new File("voc.json");
	
	private final @NotNull List<Voc> voc;
	private final @NotNull Map<Long, Voc> linker;
	private final @NotNull Set<Long> ids;
	
	public VocService() {
		this.voc = _read(file)
				.orElse(new ArrayList<>());
		this.linker = new HashMap<>();
		this.ids = new HashSet<>();
		
		for (Voc v : voc) {
			this.linker.put(v.getId(), v);
			this.ids.add(v.getId());
			
			
		}
		
		save();
	}
	
	@Override
	public @NotNull Optional<Voc> findByID(long id) {
		if (!ids.contains(id)) return Optional.empty();
		
		return Optional.of(this.linker.get(id));
	}
	
	@Override
	public void update(@NotNull Voc data) {
		voc.replaceAll(v -> {
			if (v.getId() == data.getId()) return data;
			else return v;
		});
		
		linker.replace(data.getId(), data);
		
		save();
	}
	
	@Override
	public void insert(@NotNull Voc data) {
		voc.add(data);
		linker.put(data.getId(), data);
		ids.add(data.getId());
		
		save();
	}
	
	@Override
	public void remove(@NotNull Voc data) {
		ids.remove(data.getId());
		linker.remove(data.getId());
		voc.remove(data);
		
		save();
	}
	
	@Override
	public boolean exist(long id) {
		return ids.contains(id);
	}
	
	@Override
	public Stream<Voc> stream() {
		return voc.stream();
	}
	
	@Override
	public Stream<Voc> parallel() {
		return voc.parallelStream();
	}
	
	@Override
	public void save() {
		_write(file, voc);
	}
	
	@Override
	public Class<Voc> getClazz() {
		return Voc.class;
	}
}
