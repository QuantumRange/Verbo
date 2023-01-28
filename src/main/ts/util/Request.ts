import {AnswerClassification, LearningMode, Voc, VocView} from "../model/Vocabulary.js";
import {VocSet} from "../model/VocabularySet.js";
import {debug} from "./Debug.js";

function req(path: string, data?: string | null, cacheable: boolean = true): Promise<string> {
	debug(" << ", path, " with data: '", data, "'");

	return new Promise(function (resolve, reject) {
		let req: Promise<Response>;

		if (data) {
			req = fetch(path, {
				method: 'POST',
				body: data,
				headers: {
					'Content-Type': 'application/json'
				}
			});
		} else {
			req = fetch(path, {
				method: 'POST',
			});
		}

		req.then(response => {
			response.text().then(text => {
				debug(" >> ", path, " with data: '", text.trim(), "'");
				resolve(text);
			}).catch(reject);
		}).catch(reject);
	})
}

function parseVoc(json: any): Voc {
	let views: VocView[] = [];

	for (let i = 0; i < json['views'].length; i++) {
		views.push(parseView(json['views'][i]));
	}

	return new Voc(json['id'],
		json['question'],
		json['answer'],
		json['questionLanguage'],
		json['answerLanguage'],
		views);
}

function parseView(json: any): VocView {
	return new VocView(Number(json['timestamp']),
		json['answer'],
		parseInt(json['correctness']),
		parseInt(json['classification']),
		parseInt(json['mode']),
		parseInt(json['responseTime']),
		json['reversed'] === "true");
}

export interface LearnResult {
	snowflake: string;
	timestamp: number;
	answer: string;
	classification: AnswerClassification;
	mode: LearningMode;
	responseTime: number;
	reversed: boolean;
}

export function responseLearn(result: LearnResult): Promise<void> {
	return new Promise<void>((resolve, reject) => {
		req("/api/learn", JSON.stringify(result)).then(response => {
			if (response === "true") resolve();
			else reject("Wrong response from server: " + response);
		}, reason => reject(reason));
	});
}

export function requestVoc(id: string): Promise<Voc> {
	return new Promise<Voc>((resolve, reject) => {
		req("/api/voc", id, true).then(rawJSON => {
			resolve(parseVoc(JSON.parse(rawJSON)));
		}).catch(reason => reject(reason));
	});
}

export function requestVocDelete(setId: string, vocId: string): Promise<boolean> {
	return new Promise<boolean>((resolve, reject) => {
		req("/api/voc/delete", JSON.stringify({
			set: setId,
			voc: vocId
		}), true).then((rawJson: string) => {
			resolve(true);
		}).catch(reason => reject(reason));
	});
}

export function requestVocEdit(setId: string, vocId: string, newQuestion: string, newAnswer: string): Promise<boolean> {
	return new Promise<boolean>((resolve, reject) => {
		req("/api/voc/edit", JSON.stringify({
			set: setId,
			voc: vocId,
			newQuestion: newQuestion,
			newAnswer: newAnswer
		}), true).then((rawJson: string) => {
			resolve(true);
		}).catch(reason => reject(reason));
	});
}

export function requestSet(id: string): Promise<VocSet> {
	return new Promise<VocSet>((resolve, reject) => {
		req("/api/set", id, true).then(rawJSON => {
			let json = JSON.parse(rawJSON);

			let vocList: Voc[] = [];

			for (let i = 0; i < json['vocabularies'].length; i++) {
				vocList.push(parseVoc(json['vocabularies'][i]));
			}

			resolve(new VocSet(BigInt(json['id']),
				json['name'],
				BigInt(json['owner']),
				vocList));
		}).catch(reason => reject(reason));
	});
}

export function requestConfig(): Promise<Map<string, object>> {
	return new Promise((resolve, reject) => {
		req("/api/user/request", null, false).then(value => {
			const jsonObject = JSON.parse(value);
			resolve(new Map(Object.keys(jsonObject).map(key => [key, jsonObject[key]])));
		}).catch(reason => reject(reason));
	});
}

export function updateConfig(key: string, value: any): Promise<void> {
	return new Promise((resolve, reject) => {
		req("/api/user/update", JSON.stringify({
			"key": key,
			"value": value
		}), false).then(_ => {
			resolve();
		}).catch(reason => reject(reason));
	});
}