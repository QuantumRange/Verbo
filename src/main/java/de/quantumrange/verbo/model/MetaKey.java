package de.quantumrange.verbo.model;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static de.quantumrange.verbo.model.MetaKey.MetaFlag.CLIENT;
import static java.util.EnumSet.of;

public class MetaKey<T> {
	
	public static final MetaKey<String> COLOR_THEME = new MetaKey<>("COLOR_THEME",
			"Color Theme",
			"Intense Focus",
			s -> s,
			String.class,
			of(CLIENT, MetaFlag.UPDATE_BY_USER));
	public static final MetaKey<Float> FONT_SIZE = new MetaKey<>("FONT_SIZE",
			"Font size",
			1.3f, //rem
			Float::parseFloat,
			float.class,
			of(CLIENT, MetaFlag.UPDATE_BY_USER));
	public static final MetaKey<Boolean> FORCE_PASSWORD_CHANGE = new MetaKey<>("FORCE_PASSWORD_CHANGE",
			"Force Password Change",
			false,
			Boolean::parseBoolean,
			Boolean.class,
			of(MetaFlag.PRIVATE));
	public static final MetaKey<Integer> LEARNING_LOOP_AMOUNT = new MetaKey<>("LEARNING_LOOP_AMOUNT",
			"Learning Loop Amount",
			5,
			Integer::parseInt,
			Integer.class,
			of(MetaFlag.CLIENT, MetaFlag.UPDATE_BY_USER));
	
	public static final MetaKey<String> LEARNING_SETTINGS = new MetaKey<>("LEARNING_SETTINGS",
			"Internal stuff",
			"null",
			s -> s,
			String.class,
			of(MetaFlag.CLIENT, MetaFlag.UPDATE_BY_USER, MetaFlag.PRIVATE));
	
	public static final MetaKey<?>[] values = new MetaKey[]{
			COLOR_THEME,
			FONT_SIZE,
			FORCE_PASSWORD_CHANGE,
			LEARNING_LOOP_AMOUNT,
			LEARNING_SETTINGS
	};
	
	private final String name;
	private final String display;
	private final T defaultValue;
	private final Function<String, T> updateParser;
	private final Class<? extends T> clazz;
	
	private final Set<MetaFlag> flags;
	
	public MetaKey(String name, String display, T defaultValue, Function<String, T> updateParser, Class<T> clazz, Set<MetaFlag> flags) {
		this.name = name;
		this.display = display;
		this.defaultValue = defaultValue;
		this.updateParser = updateParser;
		this.clazz = clazz;
		this.flags = flags;
	}
	
	public T unpackObject(String value) {
		return updateParser.apply(value);
	}
	
	public void set(User user, String value) {
		user.set(this, updateParser.apply(value));
	}
	
	public Class<? extends T> getClazz() {
		return clazz;
	}
	
	public Set<MetaFlag> getFlags() {
		return flags;
	}
	
	public Function<String, T> getUpdateParser() {
		return updateParser;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MetaKey<?> metaKey = (MetaKey<?>) o;
		return Objects.equals(getDisplay(), metaKey.getDisplay()) && Objects.equals(getDefaultValue(), metaKey.getDefaultValue());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getDisplay(), getName(), getDefaultValue());
	}
	
	public enum MetaFlag {
		
		PUBLIC,
		CLIENT,
		UPDATE_BY_USER,
		PRIVATE
		
	}
	
}
