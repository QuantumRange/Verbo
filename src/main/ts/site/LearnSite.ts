import {AnswerClassification, LearningMode, Voc, VocView} from "../model/Vocabulary.js";
import {LearnResult, requestSet, responseLearn} from "../util/Request.js";
import {VocSet} from "../model/VocabularySet.js";
import {setProgress, shuffle} from "../util/HTMLUtil.js";
import {isLearned, getLeastKnownVoc, damerauLevenshteinDistance, isValidAnswer} from "../util/LearnUtil.js";
import {confGetOr} from "../util/Config.js";
import {debug} from "../util/Debug.js";

enum LearnState {

	DISABLED,
	TEXT,
	TEXT_WRONG,
	CARD_FIRST,
	CARD_WRONG,

}

let serverSyncDiv: HTMLSpanElement;
let progressBar: HTMLDivElement;
let syncingBar: HTMLDivElement;

let lang: {
	question: HTMLSpanElement,
	answer: HTMLSpanElement,
	button: HTMLButtonElement
};

let views: {
	card: {
		div: HTMLDivElement,
		question: HTMLTitleElement,
		countdown: {
			wrapper: HTMLTitleElement,
			text: HTMLElement,
			skipBtn: HTMLButtonElement
		},
		answer: HTMLTitleElement[] // cardAnswer0, ..., cardAnswer3
	},
	text: {
		div: HTMLDivElement,
		question: HTMLTitleElement,
		warning: {
			div: HTMLDivElement,
			rightAnswer: HTMLTitleElement,
			skipBtn: HTMLButtonElement
		},
		input: {
			input: HTMLInputElement,
			answerBtn: HTMLButtonElement
		}
	}
};


/**
 * How many times a text input must be false for the vocabulary to switched to card state.
 */
const TO_CARD_LIMIT: number = 3;

/**
 * How many times a user must see a card for the card question to be a text question.
 */
const TO_TEXT_LIMIT: number = 1;

/**
 * Defines the size of the pack array.
 * With isState more described in {@link pack}.
 */
const PACK_SIZE: number = parseInt(confGetOr('LEARNING_LOOP_AMOUNT', "5"));


/**
 * All vocabularies that are in temp learning state.
 * So these vocabularies repeat until one of them isState learned a gets replaced with a new one.
 * The size of the array isState always {@link PACK_SIZE}, except at start-up.
 */
let pack: Voc[] = [];

/**
 * The next displayed word.
 * The number gets from 0 (before first word isState shown) to {@link PACK_SIZE}.
 */
let packPointer: number = 0;

let current: Voc | null = null;
let answerBuffer: string | null;
let displayedTimestamp: number = 0;

/**
 * Describes the index of the card with the right answer
 */
let cardAnswerIndex: number | null;

let reversed: boolean = false;

/**
 * Packs ready to send to server for sync.
 */
let serverQueue: LearnResult[] = [];
let serverQueueMax: number = 0;

let setID: string;
let options: LearningMode[] = [];
let currentMode: LearningMode;
let set: VocSet;
let state: LearnState = LearnState.DISABLED;


function saveQueue() {
	try {
		localStorage.setItem("requestCache", JSON.stringify(serverQueue));
	} catch (error) {
		console.error("Encode queue: ", error);
	}
}

function queueVocResponse(answer: string, mode: LearningMode, classification: AnswerClassification) {
	let response: LearnResult = {
		mode: mode,
		answer: answer,
		classification: classification,
		reversed: reversed,
		responseTime: Date.now() - displayedTimestamp,
		snowflake: current.id,
		timestamp: displayedTimestamp
	};

	if (answer === null || answer === undefined)
		throw new Error('anwser is null');

	serverSyncDiv.style.display = '';
	serverQueueMax++;

	current.views.push(new VocView(response.timestamp,
		answer,
		damerauLevenshteinDistance(answer, reversed ? current.answer : current.question),
		classification,
		mode,
		response.responseTime,
		reversed));

	serverQueue.push(response);
	saveQueue();

	setProgress(syncingBar, (serverQueueMax - serverQueue.length) / (Math.max(serverQueueMax - 1, 1)));

	if (serverQueueMax == 1) {
		_refreshQueue();
	}
}

function _refreshQueue() {
	if (serverQueue.length === 0) {
		serverSyncDiv.style.display = 'none';
		serverQueueMax = 0;
		return;
	}

	const resp = serverQueue[0];

	setProgress(syncingBar, (serverQueueMax - serverQueue.length) / (Math.max(serverQueueMax - 1, 1)));

	responseLearn(resp).then(_ => {
		serverQueue.splice(0, 1);
		saveQueue();
		debug("{", resp, "} Send!");
		_refreshQueue();
	}, err => {
		debug(err);
		setTimeout(() => {
			_refreshQueue();
		}, 5000);
	});
}


function displayText(voc: Voc) {
	views.text.input.input.classList.remove("answer-wrong");
	views.card.div.style.display = 'none';
	views.text.div.style.display = '';
	views.text.warning.div.style.display = 'none';

	views.text.question.innerText = reversed ? voc.answer : voc.question;

	views.text.input.input.value = '';
	views.text.input.input.focus();

	views.text.input.answerBtn.disabled = false;
	views.text.input.answerBtn.innerText = 'Answer';

	answerBuffer = null;

	state = LearnState.TEXT;
}

function selectText() {
	if (!anyState([LearnState.TEXT])) return;

	let answer = views.text.input.input.value.trim();

	views.text.input.input.value = answer;
	answerBuffer = answer;

	let solution = reversed ? current.question : current.answer;

	if (isValidAnswer(answer, solution, true)) {
		queueVocResponse(answerBuffer, LearningMode.TEXT, AnswerClassification.RIGHT);
		state = LearnState.DISABLED;

		setTimeout(() => {
			next();
		}, 300);
	} else {
		views.text.warning.div.style.display = '';
		views.text.input.input.value = '';
		views.text.warning.skipBtn.style.display = '';
		views.text.warning.rightAnswer.innerText = solution;
		views.text.input.answerBtn.innerText = 'Check';
		state = LearnState.TEXT_WRONG;
	}
}


function displayCard(voc: Voc) {
	// Remove deco
	for (let i = 0; i < views.card.answer.length; i++) {
		views.card.answer[i]
			.parentElement
			.parentElement
			.classList
			.remove('bg-success', 'bg-warning', 'bg-danger');
	}

	// Display
	views.card.div.style.display = '';
	views.text.div.style.display = 'none';

	views.card.question.innerText = reversed ? voc.answer : voc.question;
	views.card.countdown.wrapper.style.display = 'none';

	let answers: string[] = [];

	const solution = reversed ? voc.question : voc.answer;
	answers.push(solution);

	for (let i = 0; i < 3; i++) {
		const vocabulary = set.vocabularies[Math.floor(Math.random() * set.vocabularies.length)];
		const wrongAnswer = reversed ? vocabulary.question : vocabulary.answer;

		if (!answers.includes(wrongAnswer)) {
			answers.push(wrongAnswer);
		} else {
			i--;
		}
	}

	answers = shuffle(answers);

	for (let i = 0; i < views.card.answer.length; i++) {
		views.card.answer[i].innerText = answers[i];

		if (answers[i] == solution) {
			cardAnswerIndex = i;
		}
	}

	state = LearnState.CARD_FIRST;
}

function selectCard(id: number) {
	if (!anyState([LearnState.CARD_FIRST, LearnState.CARD_WRONG])) return;

	const isCorrect = id == cardAnswerIndex;

	if (isState(LearnState.CARD_FIRST)) {
		queueVocResponse(views.card.answer[id].innerText, LearningMode.CARD, isCorrect ? AnswerClassification.RIGHT : AnswerClassification.WRONG);
	}

	state = LearnState.DISABLED;

	const classList = views.card.answer[id]
		.parentElement
		.parentElement
		.classList;

	if (id == cardAnswerIndex) {
		classList.add('bg-success');
	} else {
		classList.add('bg-danger');
	}

	if (isCorrect) {
		setTimeout(() => {
			for (const item of views.card.answer) {
				item
					.parentElement
					.parentElement
					.classList
					.remove('bg-success', 'bg-warning', 'bg-danger');
			}
			next();
		}, isCorrect ? 500 : 2000);
	} else {
		setTimeout(() => {
			state = LearnState.CARD_WRONG;
		}, 250);
	}
}

function next() {
	(reversed ? lang.answer : lang.question).innerText = set.vocabularies[0].questionLang.toString();
	(reversed ? lang.question : lang.answer).innerText = set.vocabularies[0].answerLang.toString();

	if (packPointer === PACK_SIZE) {
		const exclude = pack.map(value => value.id);

		for (let i = 0; i < PACK_SIZE; i++) {
			if (isLearned(pack[i], options)) {
				pack[i] = getLeastKnownVoc(set, options, exclude).voc;
				exclude.push(pack[i].id);
			}
		}

		pack = shuffle(pack);

		packPointer = 0;
	}

	let voc = pack[packPointer];
	let mode: LearningMode = LearningMode.CARD;

	// --- Check then state should be text state ---
	let rightAnswers = 0;

	for (let i = 0; i < TO_CARD_LIMIT; i++) {
		let id: number = voc.views.length - i - 1;

		if (id < 0) {
			continue;
		}

		if (voc.views[id].classification !== AnswerClassification.WRONG) {
			rightAnswers++;
		}
	}

	if (voc.views.length >= TO_TEXT_LIMIT /* If seen at least ? times  */
		&& rightAnswers >= TO_TEXT_LIMIT  /* If less than ? times wrong in the last 3 tries */) {
		mode = LearningMode.TEXT;
	}

	setProgress(progressBar, (packPointer + 1) / PACK_SIZE);

	current = voc;
	displayedTimestamp = Date.now();

	// TODO: Make prettier
	if (mode == LearningMode.CARD && !options.includes(LearningMode.CARD)) {
		mode = LearningMode.TEXT;
	} else if (mode == LearningMode.TEXT && !options.includes(LearningMode.TEXT)) {
		mode = LearningMode.CARD;
	}

	currentMode = mode;

	switch (mode) {
		case LearningMode.CARD:
			displayCard(voc);
			break;
		case LearningMode.TEXT:
			displayText(voc);
			break;
		default:
			debug("Can't work with LearningMode: " + mode + " and options: " + options);
			break;
	}

	packPointer++;
}

window.addEventListener('load', () => {
	setID = document.getElementById('set').innerText;

	let optionElement: HTMLSpanElement = document.getElementById('options');

	for (let str of optionElement.textContent.split(";")) {
		options.push(parseInt(str));
	}

	serverSyncDiv = document.getElementById('syncWithServer') as HTMLSpanElement;
	progressBar = document.getElementById('processBar') as HTMLDivElement;
	syncingBar = document.getElementById('syncingBar') as HTMLDivElement;

	lang = {
		question: document.getElementById('questionLang') as HTMLSpanElement,
		answer: document.getElementById('answerLang') as HTMLSpanElement,
		button: document.getElementById('toggleReverse') as HTMLButtonElement
	};

	views = {
		card: {
			div: document.getElementById('learn-card') as HTMLDivElement,
			countdown: {
				wrapper: document.getElementById('countdownHead') as HTMLTitleElement,
				skipBtn: document.getElementById('skipCountdown') as HTMLButtonElement,
				text: document.getElementById('countdown')
			},
			question: document.getElementById('cardQuestion') as HTMLTitleElement,
			answer: [
				document.getElementById('cardAnswer0') as HTMLTitleElement,
				document.getElementById('cardAnswer1') as HTMLTitleElement,
				document.getElementById('cardAnswer2') as HTMLTitleElement,
				document.getElementById('cardAnswer3') as HTMLTitleElement
			]
		},
		text: {
			div: document.getElementById('learn-text') as HTMLDivElement,
			question: document.getElementById('text-name') as HTMLTitleElement,
			warning: {
				div: document.getElementById('text-alt') as HTMLDivElement,
				rightAnswer: document.getElementById('text-answer') as HTMLTitleElement,
				skipBtn: document.getElementById('skip-btn') as HTMLButtonElement
			},
			input: {
				input: document.getElementById('inputVal') as HTMLInputElement,
				answerBtn: document.getElementById('text-btn') as HTMLButtonElement
			}
		}
	};

	(document.getElementById('charA') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('á'));
	(document.getElementById('charE') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('é'));
	(document.getElementById('charI') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('í'));
	(document.getElementById('charU') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('ú'));
	(document.getElementById('charO') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('ó'));
	(document.getElementById('charN') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('ñ'));
	(document.getElementById('charAE') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('ä'));
	(document.getElementById('charUE') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('ü'));
	(document.getElementById('charOE') as HTMLButtonElement)
		.addEventListener('click', () => typeChar('ö'));

	views.text.input.input.addEventListener('keydown', (e) => {
		if (isState(LearnState.DISABLED)) return;

		if (e.key === 'Enter') {
			selectText();
		}
	});

	lang.button.addEventListener('click', () => {
		if (isState(LearnState.DISABLED)) return;

		state = LearnState.DISABLED;
		reversed = !reversed;

		resetVoc();

		packPointer = PACK_SIZE;
		next();
	});

	// Stop form submit event on views.text.warning.div.parentElement
	views.text.warning.div.parentElement.addEventListener('submit', (e) => {
		e.preventDefault();

		if (isState(LearnState.DISABLED)) return;

		if (currentMode == LearningMode.TEXT && answerBuffer === null && state == LearnState.TEXT) {
			state = LearnState.DISABLED;
			setTimeout(() => selectText(), 300);
		} else if (isState(LearnState.TEXT_WRONG) && answerBuffer !== null) {
			const solution = reversed ? current.question : current.answer;

			if (isValidAnswer(views.text.input.input.value, solution, false)) {
				state = LearnState.DISABLED;

				queueVocResponse(answerBuffer, LearningMode.TEXT, AnswerClassification.WRONG);

				setTimeout(() => next(), 300);
			}
		}
		return true;
	});

	views.text.input.input.addEventListener('keyup', () => {
		if (!isState(LearnState.TEXT_WRONG)) return;
		const solution = reversed ? current.question : current.answer;

		if (!isValidAnswer(views.text.input.input.value, solution, false)) {
			views.text.input.input.classList.add("answer-wrong");
			return;
		}

		views.text.input.input.classList.remove("answer-wrong");
	});

	for (let cardElement of views.card.answer) {
		cardElement.parentElement.addEventListener('click', _ => {
			selectCard(Number(cardElement.id[cardElement.id.length - 1]));
		});
	}

	views.text.div.style.display = 'none';
	views.card.div.style.display = 'none';
	views.text.input.answerBtn.innerText = 'Next';
	progressBar.style.display = '';

	requestSet(setID).then(_set => {
		set = _set;

		resetVoc();

		next();

		serverSyncDiv.style.display = 'none';

		// Load Queue
		const requestCache = localStorage.getItem("requestCache");

		if (requestCache === null) {
			return;
		}

		try {
			serverQueue = JSON.parse(requestCache);
		} catch (error) {
			serverQueue = [];
		}
		serverQueueMax = serverQueue.length;
		serverSyncDiv.style.display = '';
		_refreshQueue();
	}).catch(reason => {
		debug('Error: ' + reason);
	});
});

document.addEventListener('keyup', (e) => {
	if (isState(LearnState.DISABLED)) return;

	if (e.code.endsWith("1")) selectCard(0);
	if (e.code.endsWith("2")) selectCard(1);
	if (e.code.endsWith("3")) selectCard(2);
	if (e.code.endsWith("4")) selectCard(3);
});

const pattern = RegExp("[^a-z ]");

document.addEventListener('click', (e) => {
	if (isState(LearnState.DISABLED)) return;

	if (e.target instanceof HTMLButtonElement && e.target.id === 'skip-btn' && isState(LearnState.TEXT_WRONG)) {
		state = LearnState.DISABLED;
		queueVocResponse(answerBuffer, LearningMode.TEXT, AnswerClassification.MARKED_AS_RIGHT);
		setTimeout(() => {
			next();
		}, 250);
	}
});

function resetVoc() {
	const exclude: string[] = [];
	pack = [];

	for (let i = 0; i < PACK_SIZE; i++) {
		const voc = getLeastKnownVoc(set, options, exclude);
		pack.push(voc.voc);
		exclude.push(voc.voc.id);
	}
}

function isState(_state: LearnState) {
	return state === _state;
}

function anyState(states: LearnState[]) {
	for (let _state of states) {
		if (isState(_state)) return true;
	}

	return false;
}

function typeChar(char: string) {
	if (isState(LearnState.DISABLED)) return;

	views.text.input.input.value += char;
	views.text.input.input.focus();
}