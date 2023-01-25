import {requestVocDelete} from "../util/Request.js";
import {attachEvent} from "../util/HTMLUtil.js";
import {debug} from "../util/Debug.js";

let setId: string;

window.addEventListener('load', () => {
	setId = document.getElementById('visibleID').innerText.trim();

	attachEvent('ts-delete-action', 'click', element => onDelete(element));
	attachEvent('ts-edit-action', 'click', element => onEdit(element));
	attachEvent('ts-mark-action', 'click', element => onMark(element));
});

function onDelete(element: HTMLElement | Element) {
	const vocTr: HTMLElement = element.parentElement.parentElement;
	const visibleId: HTMLTableCellElement = vocTr.getElementsByClassName('ts-visible-id')[0] as HTMLTableCellElement;

	requestVocDelete(setId, visibleId.innerText)
		.then(value => {
			if (value) {
				vocTr.remove();
			}
		}).catch(reason => debug(reason));
}

function onEdit(element: HTMLElement | Element) {
	const vocTr: HTMLElement = element.parentElement.parentElement;
	const visibleId: HTMLTableCellElement = vocTr.getElementsByClassName('ts-visible-id')[0] as HTMLTableCellElement;

	const questionTd: HTMLTableCellElement = vocTr.getElementsByClassName('ts-question')[0] as HTMLTableCellElement;
	const answerTd: HTMLTableCellElement = vocTr.getElementsByClassName('ts-answer')[0] as HTMLTableCellElement;

	const question = questionTd.innerText;
	const answer = questionTd.innerText;

	function createInput(node: HTMLElement,
	                     text: string) {
		const inputElement: HTMLInputElement = document.createElement('input');

		inputElement.type = 'text';
		inputElement.classList.add('form-text');
		inputElement.value = text;

		node.innerHTML = '';
		node.appendChild(inputElement);
	}

	createInput(questionTd, question);
	createInput(answerTd, answer);

	// TODO: Change
	// requestVocDelete(setId, visibleId.innerText);
}

function onMark(element: HTMLElement | Element) {
	const vocTr: HTMLElement = element.parentElement.parentElement;
	const visibleId: HTMLTableCellElement = vocTr.getElementsByClassName('ts-visible-id')[0] as HTMLTableCellElement;

	// TODO: Change
	requestVocDelete(setId, visibleId.innerText);

	window.location.reload();
}