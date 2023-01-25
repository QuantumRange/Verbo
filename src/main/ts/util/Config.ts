import {requestConfig, updateConfig} from "./Request.js";
import {debug} from "./Debug.js";

let CONFIG: Map<string, object> = new Map<string, object>();
let hash: string;

const localData = window.localStorage.getItem("data");
if (localData !== null) {
	try {
		const jsonText = atob(localData);
		const jsonObject = JSON.parse(jsonText);
		CONFIG = new Map(jsonObject);
	} catch (error) {
		console.error("Parse config: ", error);
		CONFIG = new Map();
		window.localStorage.setItem("hash", "error");
	}
}

async function reloadConfig(): Promise<void> {
	let value: Map<string, object> = await requestConfig();

	window.localStorage.setItem("data", btoa(JSON.stringify(Array.from(value))));
	window.localStorage.setItem("hash", String(hash));

	CONFIG = value;
}

export function confSet(key: string, value: any): Promise<void> {
	CONFIG.set(key, value);

	window.localStorage.setItem("data", btoa(JSON.stringify(Array.from(CONFIG))));
	window.localStorage.setItem("hash", "invalid");
	return updateConfig(key, value);
}

export function confGet(key: string): object {
	return CONFIG.get(key);
}

export function confGetOr<T>(key: string, defaultValue: T): T {
	let val = confGet(key);

	if (val === null
		|| val === undefined
		|| (defaultValue instanceof Object && !(val instanceof defaultValue.constructor))) {
		return defaultValue;
	}

	return val as T;
}

window.addEventListener('load', async function () {
	const hashElement = document.getElementById("prefHash");

	if (hashElement === null) return;
	if (!(hashElement instanceof HTMLInputElement)) return;

	hash = hashElement.value;

	if (localData !== null) {
		let oldHash = window.localStorage.getItem("hash");

		if (oldHash != hash) {
			debug("Cache invalid! ", hash, "!=", oldHash);
			await reloadConfig();
		}
	} else await reloadConfig();
});