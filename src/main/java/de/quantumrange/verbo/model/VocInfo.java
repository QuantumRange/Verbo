package de.quantumrange.verbo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class VocInfo {
	
	private long snowflake;
	private int correct, wrong;
	private @NotNull Set<VocView> views;
	private boolean starred;
	private @NotNull String note;
	
}