package de.quantumrange.verbo.model;

import lombok.Getter;

/**
 * TODO: Replace a const enum with a more dynamic approach
 */
@Deprecated
@Getter
public enum Language {
	
	GERMAN("German"),
	ENGLISH("English"),
	SPANISH("Spanish"),
	FRENCH("French");
	
	private final String name;
	
	Language(String name) {
		this.name = name;
	}
	
}
