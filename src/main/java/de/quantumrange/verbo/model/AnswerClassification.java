package de.quantumrange.verbo.model;

public enum AnswerClassification {

	WRONG(false),
	RIGHT(true),
	MARKED_AS_RIGHT(true);

	private final boolean correct;

	AnswerClassification(boolean correct) {
		this.correct = correct;
	}

	public boolean isCorrect() {
		return correct;
	}
}
