package de.quantumrange.verbo.service;

import de.quantumrange.verbo.model.VocSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

@Service
public class VocSetService implements DataService<VocSet> {

	private static final File file = new File("sets.json");

	private final @NotNull List<VocSet> sets;
	private final @NotNull Map<Long, VocSet> linker;

	public VocSetService() {
		this.sets = _read(file)
				.orElse(new ArrayList<>());
		linker = new HashMap<>();

		for (VocSet set : sets) {
			linker.put(set.getId(), set);
		}

		save();
	}

	@Override
	public @NotNull Optional<VocSet> findByID(long id) {
		return Optional.ofNullable(linker.getOrDefault(id, null));
	}

	@Override
	public void update(@NotNull VocSet data) {
		sets.replaceAll(vocSet -> {
			if (vocSet.getId() == data.getId()) {
				return data;
			} else return vocSet;
		});
		linker.replace(data.getId(), data);

		save();
	}

	@Override
	public void insert(@NotNull VocSet data) {
		sets.add(data);
		linker.put(data.getId(), data);

		save();
	}

	@Override
	public void remove(@NotNull VocSet data) {
		sets.remove(data);
		linker.remove(data.getId());

		save();
	}

	@Override
	public boolean exist(long id) {
		return linker.containsKey(id);
	}

	@Override
	public Stream<VocSet> stream() {
		return sets.stream();
	}

	@Override
	public Stream<VocSet> parallel() {
		return sets.parallelStream();
	}

	@Override
	public void save() {
		_write(file, sets);
	}

	@Override
	public Class<VocSet> getClazz() {
		return VocSet.class;
	}
}
