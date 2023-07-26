import {Voc} from "../model/Vocabulary";

window.addEventListener('load', ev => {
	let fileSelector = document.getElementById('file') as HTMLInputElement;

	fileSelector.addEventListener('change', e => {
		// @ts-ignore
		const file = e.target.files[0];

		if (!file) {
			return;
		}
		const reader = new FileReader();

		reader.onload = function(e) {
			const contents = e.target.result;

			if (typeof contents === "string") {
				fileLoaded(fileSelector.value.substring(fileSelector.value.lastIndexOf('.') + 1), contents as string);
			} else {
				console.error("Error, file isn't a string");
			}
		};

		reader.readAsText(file);
	});
});

function fileLoaded(ending: string, content: string) {
	let patternDiv = document.getElementById("pattern") as HTMLDivElement;
	patternDiv.style.display = "";


}

type Voc = {
	left: string,
	right: string
};

abstract class Pattern {

	name: string

	protected constructor(name: string) {
		this.name = name;
	}

	abstract load(): void;
	abstract unload(): void;

	abstract importSet(content: string): Array<Voc>;

}

// X Seperated Values
class XSV extends Pattern {
// TODO: Continue here
	constructor() {
		super("x");
	}

	load(): void {
	}

	unload(): void {
	}

	importSet(content: string): Array<Voc> {
		return undefined;
	}


}