import {AnswerClassification, LearningMode, Voc} from "../model/Vocabulary.js";
import {VocSet} from "../model/VocabularySet.js";
import {confGetOr} from "./Config.js";
import {debug} from "./Debug.js";

export class RatingWeights {

	// High = Making often errors will rate higher
	// Low = Often making mistakes doesn't matter
	recurrenceRate: number;

	// Higher = Typos are rated extremely high
	// Lower = Typos are not important
	typos: number;

	// Higher = more unknown
	// Lower = unknown after learning the current once's very good
	unknown: number;

	// Higher = Punishment for being slow
	// Lower = !Higher
	speed: number;

	// Speed goal for speed in ms
	speedGoal: number;

	constructor(recurrenceRate: number, typos: number, unknown: number, speed: number, speedGoal: number) {
		this.recurrenceRate = recurrenceRate;
		this.typos = typos;
		this.unknown = unknown;
		this.speed = speed;
		this.speedGoal = speedGoal;
	}

}

export const DEFAULT_WEIGHTS: RatingWeights = new RatingWeights(80, 2, 50, 1, 3000);

const data = confGetOr<string>('LEARNING_SETTINGS', JSON.stringify(DEFAULT_WEIGHTS));
const weight = data === "null" ? DEFAULT_WEIGHTS : JSON.parse(data) as RatingWeights;

function score(voc: Voc, options: LearningMode[], weights: RatingWeights): number {
	let score: number = 0.0;

	if (voc.views.length === 0) return Number.MAX_VALUE;

	// Prepare data
	const HISTORY_LIMIT = 5;
	const learnedHistory = simplifyHistory(voc, HISTORY_LIMIT);

	if (voc.views.length < HISTORY_LIMIT) {
		score += weights.unknown * curve(HISTORY_LIMIT - voc.views.length, 1, 20);
		return score;
	}

	for (let i = 0; i < HISTORY_LIMIT; i++) {
		const viewID = voc.views.length - i - 1;
		const view = voc.views[viewID];

		const multiplier = curve(i, 1, 4);

		if (!learnedHistory[i]) {
			score += weights.recurrenceRate * multiplier;
		}

		if (view.mode === LearningMode.TEXT) {
			// between 0 and 1
			const incorrectness = view.correctness / (view.reversed ? voc.question : voc.answer).length;

			score += weights.typos * incorrectness * multiplier;
		}

		const TIME = weights.speedGoal * (view.mode == LearningMode.TEXT ? 1 : 0.5);

		const toSlow = TIME < view.responseTime;
		if (toSlow && TIME * 4 > view.responseTime) {
			const MATCHING = (view.responseTime - TIME);

			score += weights.speed * (1 - MATCHING);
		} else if (toSlow) {
			score += weights.speed;
		}
	}

	return score;
}

export function getLeastKnownVoc(set: VocSet, options: LearningMode[], exclude: string[] = [], weights: RatingWeights = weight): { voc: Voc, value: number } {
	let scoreTable: Map<string, number> = new Map<string, number>();

	// Calculate Score
	for (let vocabulary of set.vocabularies) {
		if (exclude.includes(vocabulary.id)) continue;

		scoreTable.set(vocabulary.id, score(vocabulary, options, weights));
	}


	debug("============================");
	set.vocabularies
		.filter(vocabulary => !exclude.includes(vocabulary.id))
		.sort((a, b) => scoreTable.get(b.id) - scoreTable.get(a.id))
		.forEach(vocabulary => {
			debug(vocabulary.question + " ".repeat(Math.abs(30 - vocabulary.question.length)), " <", vocabulary.views.length, "> ", scoreTable.get(vocabulary.id));
		});
	debug("----------------------------");


	// Find best score / maximize score
	let bestKey: string = set.vocabularies[0].id;
	let bestValue: number = 0;

	scoreTable.forEach((value, key) => {
		if (value > bestValue) {
			bestValue = value;
			bestKey = key;
		}
	});

	debug("Best score:", bestValue, "=", bestKey);

	if (bestValue <= 0) {
		debug("[[ Find last learned: ", scoreTable);

		bestValue = Number.MAX_VALUE;

		// Minimize timestamp
		for (let vocabulary of set.vocabularies) {
			if (exclude.includes(vocabulary.id)) continue;

			const timestamp = vocabulary.views[vocabulary.views.length - 1].timestamp;

			if (timestamp < bestValue) {
				bestValue = timestamp;
				bestKey = vocabulary.id;
			}
		}

		debug("{voc=", bestKey, ",timestamp:", bestValue, "}");
	}

	debug("============================");

	// Find matching voc with id
	return {
		voc: set.vocabularies.find(value => value.id === bestKey),
		value: bestValue
	};
}

export function isLearned(voc: Voc, options: LearningMode[]): boolean {
	if (options.includes(LearningMode.LIVE)) return true; // TODO: Remember to adapt this for the live mode!

	const learnHistory: boolean[] = simplifyHistory(voc, 2);

	if (options.includes(LearningMode.TEXT)) {
		return learnHistory[0] && learnHistory[1];
	} else {
		return learnHistory[0];
	}
}

function simplifyHistory(voc: Voc, limit: number): boolean[] {
	const learnHistory: boolean[] = [];

	for (let i = 0; i < limit; i++) {
		const viewID = voc.views.length - i - 1;

		if ((viewID < 0 || viewID >= voc.views.length) // TODO: when true, break for faster runtime!
			|| (voc.views[viewID].classification === AnswerClassification.WRONG)) {
			learnHistory.push(false);
			continue;
		}

		learnHistory.push(true);
	}

	return learnHistory;
}

export function damerauLevenshteinDistance(str1: string, str2: string): number {
	let substitutionMatrix: number[][] = Array.from(Array(str2.length + 1), () => new Array(str1.length + 1));

	for (let i = 0; i < substitutionMatrix.length; i++) {
		substitutionMatrix[i][0] = i;
	}

	for (let i = 0; i < substitutionMatrix[0].length; i++) {
		substitutionMatrix[0][i] = i;
	}

	for (let y = 1; y < str2.length + 1; y++) {
		for (let x = 1; x < str1.length + 1; x++) {
			substitutionMatrix[y][x] = Math.min(
				substitutionMatrix[y][x - 1] + 1,
				substitutionMatrix[y - 1][x] + 1,
				substitutionMatrix[y - 1][x - 1] + (str2[y - 1] === str1[x - 1] ? 0 : 1)
			);
			if (y > 1 && x > 1 && str2[y - 1] === str1[x - 2] && str2[y - 2] === str1[x - 1]) {
				substitutionMatrix[y][x] = Math.min(substitutionMatrix[y][x], substitutionMatrix[y - 2][x - 2] + 1);
			}
		}
	}

	return substitutionMatrix[str2.length][str1.length];
}

/**
 * f(x) = a * e^-x/b
 */
function curve(x: number, a: number, b: number): number {
	return a * Math.pow(Math.E, x * -(1 / b));
}

const BRACKET_FINDER = RegExp("\\(([^)]+)\\)");
const SLASH_FINDER = RegExp("([^/ ]+\\/[^ ]+)");

export function isValidAnswer(userInput: string, solution: string, strict: boolean): boolean {
	if (!strict) {
		let ascii = RegExp("[^a-záéíúóñäüö]");

		userInput = userInput.trim().toLowerCase().replace(ascii, "");
		solution = solution.trim().toLowerCase().replace(ascii, "");
	}

	debug(userInput, "=", solution, " ( strict =", strict, ")");

	if (solution.trim().length === 0 || userInput.trim().length === 0) return false;

	function lowerCaseFirstChar(text: string): string {
		return text.charAt(0).toLowerCase() + text.substring(1);
	}

	userInput = lowerCaseFirstChar(userInput);
	solution = lowerCaseFirstChar(solution);

	if (userInput.trim() === solution.trim()) return true;
	if (!(solution.includes('(') || solution.includes('/'))) return false;

	const bracketPairs = BRACKET_FINDER.exec(solution);

	if (isValidAnswer(userInput, solution.replace(BRACKET_FINDER, ""), strict)) return true;

	if (bracketPairs) {
		for (let bracketPair of bracketPairs) {
			if (isValidAnswer(userInput, bracketPair.substring(1, bracketPair.length - 1), strict)) return true;
		}
	}

	const slashPairs = SLASH_FINDER.exec(solution);

	if (slashPairs) {
		for (let slashPair of slashPairs) {
			const split = slashPair.split('/');

			if (isValidAnswer(userInput, solution.replace(slashPair, split[0]), strict)) return true;
			if (isValidAnswer(userInput, solution.replace(slashPair, split[1]), strict)) return true;
		}
	}

	return false;
}