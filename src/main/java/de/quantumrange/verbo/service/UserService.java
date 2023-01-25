package de.quantumrange.verbo.service;

import de.quantumrange.verbo.model.MetaData;
import de.quantumrange.verbo.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.security.Principal;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Service
public class UserService implements DataService<User> {

	private static final File file = new File("users.json");
	private final @NotNull List<User> users;
	private final @NotNull Map<Long, User> idLink;
	private final @NotNull Map<String, User> nameLink;

	public UserService() {
		this.users = _read(file)
				.orElse(new ArrayList<>());
		this.idLink = new HashMap<>();
		this.nameLink = new HashMap<>();

		for (User user : users) {
			if (user.getMeta() == null) {
				user.setMeta(new MetaData());
			}
			idLink.put(user.getId(), user);
		}

		for (User user : users) {
			nameLink.put(user.getUsername(), user);
		}

		save();
	}

	public @NotNull User findByPrinciple(@Nullable Principal principal) {
		if (principal == null)
			throw new IllegalStateException("Principal is invalid!");

		return findByUsername(principal.getName())
				.orElseThrow(() -> new IllegalStateException("Username " + principal.getName() + " not exist!"));
	}

	@Override
	public @NotNull Optional<User> findByID(long id) {
		return ofNullable(idLink.getOrDefault(id, null));
	}

	public @NotNull Optional<User> findByUsername(String username) {
		return ofNullable(nameLink.getOrDefault(username, null));
	}

	@Override
	public void update(@NotNull User user) {
		users.replaceAll(u -> {
			if (u.getId() == user.getId()) return user;
			return u;
		});

		idLink.replace(user.getId(), user);
		nameLink.replace(user.getUsername(), user);

		save();
	}

	@Override
	public void insert(@NotNull User user) {
		users.add(user);
		idLink.put(user.getId(), user);
		nameLink.put(user.getUsername(), user);

		save();
	}

	@Override
	public void remove(@NotNull User user) {
		users.remove(user);
		idLink.remove(user.getId());
		nameLink.remove(user.getUsername());

		save();
	}

	@Override
	public boolean exist(long id) {
		return idLink.containsKey(id);
	}

	@Override
	public Stream<User> stream() {
		return users.stream();
	}

	@Override
	public Stream<User> parallel() {
		return users.parallelStream();
	}

	@Override
	public void save() {
		_write(file, users);
	}

	@Override
	public Class<User> getClazz() {
		return User.class;
	}

}
