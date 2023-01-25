import {DEFAULT_WEIGHTS, getLeastKnownVoc, RatingWeights} from "../util/LearnUtil.js";
import {confGetOr, confSet} from "../util/Config.js";
import {AnswerClassification, Language, LearningMode, Voc, VocView} from "../model/Vocabulary.js";
import {VocSet} from "../model/VocabularySet.js";

let weight: RatingWeights = DEFAULT_WEIGHTS;

let settingsDiv: HTMLDivElement;
let previewDiv: HTMLDivElement;


function generateList() {
	let rawData: {
		category: string,
		description: string,
		mode: LearningMode,
		history: { correctness: number[], time: number[], classification: AnswerClassification[], }
	}[] = [];

	// Typos
	for (let i = 0; i < 5; i++) {
		let data = {
			category: "Typos",
			description: `This word has been spelled correctly of the ${(parseInt((((i + 1) / 5) * 100).toString()))}% time.`,
			mode: LearningMode.TEXT,
			history: {
				time: [3000, 3000, 3000, 3000, 3000],
				correctness: Array<number>(),
				classification: Array<AnswerClassification>()
			}
		};

		for (let j = 0; j < i; j++) {
			data.history.classification.push(AnswerClassification.RIGHT);
			data.history.correctness.push(0);
		}

		for (let j = 0; j < 5 - i; j++) {
			data.history.classification.push(AnswerClassification.WRONG);
			data.history.correctness.push(10);
		}

		rawData.push(data);
	}

	// recurrence
	for (let i = 0; i < 5; i++) {
		let data1 = {
			category: "Recurrence of Difficult Words",
			description: `This word is wrong since ${i} word(s).`,
			mode: LearningMode.TEXT,
			history: {
				time: [3000, 3000, 3000, 3000, 3000],
				correctness: Array<number>(),
				classification: Array<AnswerClassification>()
			}
		};

		let data2 = JSON.parse(JSON.stringify(data1));
		data2.description = `This word is right again since ${i} word(s).`;

		for (let j = 0; j < i; j++) {
			data1.history.classification.push(AnswerClassification.WRONG);
			data1.history.correctness.push(10);
			data2.history.classification.push(AnswerClassification.RIGHT);
			data2.history.correctness.push(0);
		}

		for (let j = 0; j < 5 - i; j++) {
			data1.history.classification.push(AnswerClassification.RIGHT);
			data1.history.correctness.push(0);
			data2.history.classification.push(AnswerClassification.WRONG);
			data2.history.correctness.push(10);
		}

		rawData.push(data1);
		rawData.push(data2);
	}

	// TODO:

	let id = 0;

	rawData.forEach(value => {
		const views: VocView[] = [];

		for (let i = 0; i < 5; i++) {
			views.push(new VocView(
				0,
				"α".repeat(20),
				value.history.correctness[i],
				value.history.classification[i],
				value.mode,
				value.history.time[i],
				false
			));
		}

		vocList.push({
			voc: new Voc(
				id.toString(),
				"α".repeat(20),
				"α".repeat(20),
				Language.ENGLISH,
				Language.ENGLISH,
				views
			),
			category: value.category,
			description: value.description
		});

		id++;
	});
}

type TVoc = { voc: Voc, category: string, description: string };
const vocList: TVoc[] = [];
generateList();

window.addEventListener('load', ev => {
	settingsDiv = document.getElementById('settings') as HTMLDivElement;
	previewDiv = document.getElementById('preview') as HTMLDivElement;

	// TODO: Load current into weight
	const data = confGetOr<string>('LEARNING_SETTINGS', JSON.stringify(DEFAULT_WEIGHTS));
	weight = data === "null" ? DEFAULT_WEIGHTS : JSON.parse(data) as RatingWeights;

	const DATA: {
		name: string,
		description: string,
		value: number,
		onChange: SettingChangeEvent
	}[] = [
		{
			name: "Recurrence of Difficult Words",
			description: "This weight controls how often words that you have previously struggled with will reappear in your learning sessions. A high number means that words you have trouble with will be shown more frequently, while a low number means that your past mistakes will not have as much impact on your learning.",
			value: weight.recurrenceRate,
			onChange: value => weight.recurrenceRate = value
		},
		{
			name: "Frequency of Misspelled Words",
			description: "This weight controls how often words that you have frequently misspelled will reappear in your learning sessions. A high number means that words you have trouble spelling will be shown more frequently, while a low number means that your misspellings will not have as much impact on your learning.",
			value: weight.typos,
			onChange: value => weight.typos = value
		},
		{
			name: "Introduction of New Words",
			description: "This weight controls how often new words will appear in your learning sessions. A high number means that you will be introduced to new words more frequently, while a low number means that you will only be introduced to new words once you have mastered the current ones.",
			value: weight.unknown,
			onChange: value => weight.unknown = value
		},
		{
			name: "Answer Speed",
			description: "This weight controls the importance of your answering speed when learning. A high number means that if you are not fast enough, you will have to repeat the vocabulary again, while a low number means that your answering speed will not be taken into consideration.",
			value: weight.speed,
			onChange: value => weight.speed = value
		}
	];

	DATA.forEach(value => {
		renderSetting(
			value.name,
			value.value,
			{
				description: value.description,
				start: "Low",
				end: "High",
			},
			value.onChange
		);
	});

	document.getElementById('saveBtn').addEventListener('click', async _ => {
		await confSet('LEARNING_SETTINGS', JSON.stringify(weight));
		alert('Saved!');
	});

	updatePreview();
});

function updatePreview() {
	const tempVoc: Voc[] = vocList.map(value => value.voc);

	let render: { voc: TVoc, value: number }[] = [];
	let exclude: string[] = [];
	let maxValue: number = 1;

	while (exclude.length !== vocList.length) {
		const voc: { voc: Voc; value: number } = getLeastKnownVoc(
			new VocSet(BigInt(0), "", BigInt(0), tempVoc),
			[LearningMode.TEXT, LearningMode.CARD],
			exclude,
			weight
		);

		exclude.push(voc.voc.id);
		render.push({
			voc: vocList.find(value => value.voc.id === voc.voc.id),
			value: voc.value
		});

		if (voc.value > maxValue) {
			maxValue = voc.value;
		}
	}

	previewDiv.innerHTML = `
		<ul class="list-group list-group-flush">
		</ul>
`;

	const list = previewDiv.getElementsByTagName('ul')[0] as HTMLUListElement;

	for (let i = 0; i < Math.min(15, render.length); i++) {
		let tVoc = render[i];

		let div: HTMLDivElement = document.createElement('div');

		div.innerHTML = `
        <li class="list-group-item">
            <b>
            ${tVoc.voc.category}
			</b>
			<br/>
			${tVoc.voc.description}
			
            <div class="progress"  role="progressbar" aria-label="Example 1px high" aria-valuenow="25" style="height: 5px">
			  <div class="progress-bar" style="width: ${((tVoc.value / maxValue) * 100)}%"></div>
			</div>
    	</li>
		`;

		div.classList.add("card");

		list.appendChild(div);
	}
}

// Rendering
export type SettingChangeEvent = (value: number) => void;

function renderSetting(name: string,
                       value: number,
                       descriptions: { description: string, start: string, end: string },
                       onChange: SettingChangeEvent) {
	let div: HTMLDivElement = document.createElement('div');

	div.innerHTML = `
    <h4 class="form-label">
        ${name}
    </h4>
    <p class="text-muted">
        ${descriptions.description}
    </p>
    <input type="range" class="form-range" min="0" max="100" step="0.5" value="${value}">
    <div class="row align-items-start">
        <div class="col-4 text-muted text-start">
            ${descriptions.start}
        </div>
        <div class="col-4"></div>
        <div class="col-4 text-muted text-end">
            ${descriptions.end}
        </div>
    </div>
	`;

	settingsDiv.appendChild(div);

	const input: HTMLInputElement = div.getElementsByTagName('input')[0] as HTMLInputElement;

	input.addEventListener('change', ev => {
		onChange(input.valueAsNumber);
		updatePreview();
	});
}