package de.quantumrange.verbo.model;

public enum Language {

	GERMAN("German"),
	ENGLISH("English"),
	SPANISH("Spanish"),
	FRENCH("French");

	private final String name;

	Language(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
