package de.quantumrange.verbo.service;

import de.quantumrange.verbo.model.Course;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

@Service
public class CourseService implements DataService<Course> {

	private static final File file = new File("courses.json");

	private final @NotNull List<Course> courses;
	private final @NotNull Map<Long, Course> linker;

	public CourseService() {
		this.courses = _read(file)
				.orElse(new ArrayList<>());
		linker = new HashMap<>();

		for (Course set : courses) {
			linker.put(set.getId(), set);
		}
	}

	public String generateCode() {
		char[] allowed = "0123456789ABDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		StringBuilder builder;
		do {
			builder = new StringBuilder();

			for (int i = 0; i < 9; i++) {
				if (i == 4) builder.append('-');
				else builder.append(allowed[new Random().nextInt(allowed.length)]);
			}
		} while (findByCode(builder.toString()).isPresent());

		return builder.toString();
	}

	@Override
	public @NotNull Optional<Course> findByID(long id) {
		return Optional.ofNullable(linker.getOrDefault(id, null));
	}

	public @NotNull Optional<Course> findByCode(String code) {
		return parallel()
				.filter(c -> c.getCode().equalsIgnoreCase(code))
				.findFirst();
	}

	@Override
	public void update(@NotNull Course data) {
		courses.replaceAll(vocSet -> {
			if (vocSet.getId() == data.getId()) {
				return data;
			} else return vocSet;
		});
		linker.replace(data.getId(), data);

		save();
	}

	@Override
	public void insert(@NotNull Course data) {
		courses.add(data);
		linker.put(data.getId(), data);

		save();
	}

	@Override
	public void remove(@NotNull Course data) {
		courses.remove(data);
		linker.remove(data.getId());

		save();
	}

	@Override
	public boolean exist(long id) {
		return linker.containsKey(id);
	}

	@Override
	public Stream<Course> stream() {
		return courses.stream();
	}

	@Override
	public Stream<Course> parallel() {
		return courses.parallelStream();
	}

	@Override
	public void save() {
		_write(file, courses);
	}

	@Override
	public Class<Course> getClazz() {
		return Course.class;
	}
}
