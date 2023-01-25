export enum Language {
	GERMAN,
	ENGLISH,
	SPANISH,
	FRENCH
}

export const LANGUAGE_LOCAL: Map<Language, string> = new Map<Language, string>([
	[Language.GERMAN, "German"],
	[Language.ENGLISH, "English"],
	[Language.SPANISH, "Spanish"],
	[Language.FRENCH, "French"]
]);

export enum LearningMode {
	TEXT,
	CARD,
	LIVE
}

export enum AnswerClassification {
	WRONG,
	RIGHT,
	MARKED_AS_RIGHT
}

export class VocView {

	timestamp: number;
	answer: string;
	correctness: number;
	classification: AnswerClassification;
	mode: LearningMode;
	responseTime: number;
	reversed: boolean;

	constructor(timestamp: number,
	            answer: string,
	            correctness: number,
	            classification: AnswerClassification,
	            mode: LearningMode,
	            responseTime: number,
	            reversed: boolean) {
		this.timestamp = timestamp;
		this.answer = answer;
		this.correctness = correctness;
		this.classification = classification;
		this.mode = mode;
		this.responseTime = responseTime;
		this.reversed = reversed;
	}
}

export class Voc {

	id: string;
	question: string;
	answer: string;

	questionLang: Language;
	answerLang: Language;

	views: VocView[];

	constructor(id: string,
	            question: string,
	            answer: string,
	            questionLang: Language,
	            answerLang: Language,
	            views: VocView[]) {
		this.id = id;
		this.question = question;
		this.answer = answer;
		this.questionLang = questionLang;
		this.answerLang = answerLang;
		this.views = views;
	}
}