package de.quantumrange.verbo.model;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Voc implements Identifiable {

	private long id;

	private String question;
	private Language questionLang;
	private String answer;
	private Language answerLang;

	public Voc() {
	}

	public Voc(long id, String question, Language questionLang, String answer, Language answerLang) {
		this.id = id;
		this.question = question;
		this.questionLang = questionLang;
		this.answer = answer;
		this.answerLang = answerLang;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Language getQuestionLang() {
		return questionLang;
	}

	public void setQuestionLang(Language questionLang) {
		this.questionLang = questionLang;
	}

	public Language getAnswerLang() {
		return answerLang;
	}

	public void setAnswerLang(Language answerLang) {
		this.answerLang = answerLang;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Voc voc = (Voc) o;
		return getId() == voc.getId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public String toString() {
		return "Voc{" +
				"id=" + id +
				", question='" + question + '\'' +
				", questionLang=" + questionLang +
				", answer='" + answer + '\'' +
				", answerLang=" + answerLang +
				'}';
	}
}
