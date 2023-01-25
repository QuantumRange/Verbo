package de.quantumrange.verbo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static java.lang.Math.abs;

public interface DataService<T> {

	default long generateID() {
		long id;

		do {
			id = abs(new Random().nextLong());
		} while (exist(id));

		return id;
	}

	Optional<T> findByID(long id);

	void update(T data);

	void insert(T data);

	void remove(T data);

	boolean exist(long id);

	Stream<T> stream();

	Stream<T> parallel();

	void save();

	default void _write(File file, List<T> data) {
		JsonMapper mapper = new JsonMapper();

		try {
			mapper.writerFor(new TypeReference<ArrayList<T>>() {
					})
					.writeValue(file, data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	Class<T> getClazz();

	default Optional<List<T>> _read(@NotNull File file) {
		if (!file.exists()) return Optional.empty();

		JsonMapper mapper = new JsonMapper();

		try {
			List<T> list = new ArrayList<>();

			JsonNode node = mapper.readerForListOf(getClazz())
					.readTree(new FileInputStream(file));

			for (int i = 0; i < node.size(); i++) {
				list.add(mapper.readerFor(getClazz())
						.readValue(node.get(i)));
			}

			return Optional.of(list);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
