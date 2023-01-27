package de.quantumrange.verbo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetaKey {
	
	FORCE_PASSWORD_CHANGE("force_password_change"),
	LEARNING_SETTINGS("learning_settings"),
	LEARNING_LOOP_AMOUNT("learning_loop_amount");
	
	private final String mapKey;
	
}
