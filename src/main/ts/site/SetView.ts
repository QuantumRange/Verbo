import {requestVocDelete, requestVocEdit} from "../util/Request.js";
import {attachEvent} from "../util/HTMLUtil.js";
import {confirmPopup, popup} from "../util/PopupUtil.js";
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

	confirmPopup('Delete word', 'This cannot be undone!', (success: boolean) => {
		if (!success) return;

		requestVocDelete(setId, visibleId.innerText)
			.then(value => {
				if (value) {
					vocTr.remove();
				}
			}).catch(reason => debug(reason));
	});
}

function onEdit(element: HTMLElement | Element) {
	const vocTr: HTMLElement = element.parentElement.parentElement;
	const visibleId: HTMLTableCellElement = vocTr.getElementsByClassName('ts-visible-id')[0] as HTMLTableCellElement;

	if (vocTr.getElementsByClassName('ts-delete-action').length === 0) {
		const questionText = (vocTr.getElementsByClassName('ts-e-question')[0] as HTMLInputElement).value;
		const answerText   = (vocTr.getElementsByClassName('ts-e-answer'  )[0] as HTMLInputElement).value;

		popup('Edit word', 'Change the word and click save.',
			[
				{
					title: 'Cancel',
					class: 'btn-secondary',
					action: () => {
						location.reload();
					}
				},
				{
					title: 'Save',
					class: 'btn-primary',
					action: async () => {
						await requestVocEdit(setId, visibleId.innerText, questionText, answerText);
						location.reload();
					}
				},
			]);
		return;
	}

	const questionTd: HTMLTableCellElement = vocTr.getElementsByClassName('ts-question')[0] as HTMLTableCellElement;
	const answerTd: HTMLTableCellElement = vocTr.getElementsByClassName('ts-answer')[0] as HTMLTableCellElement;
	const editSpan: HTMLSpanElement = vocTr.getElementsByClassName('ts-link-action-text')[0] as HTMLSpanElement;

	const editIcon = vocTr.getElementsByClassName('ts-link-action')[0].getElementsByTagName('i')[0];
	editIcon.classList.remove('fa-pen-to-square');
	editIcon.classList.add('fa-floppy-disk');

	const question = questionTd.innerText;
	const answer = answerTd.innerText;

	editSpan.innerText = 'Save';
	vocTr.getElementsByClassName('ts-delete-action')[0].remove();
	vocTr.getElementsByClassName('ts-mark-action')[0].remove();

	function createInput(node: HTMLElement,
						 className: string,
	                     text: string) {
		const inputElement: HTMLInputElement = document.createElement('input');

		inputElement.type = 'text';
		inputElement.classList.add('form-text');
		inputElement.classList.add(className);
		inputElement.value = text;

		node.innerHTML = '';
		node.appendChild(inputElement);
	}

	createInput(questionTd, 'ts-e-question', question);
	createInput(answerTd, 'ts-e-answer', answer);
}

function onMark(element: HTMLElement | Element) {
	const vocTr: HTMLElement = element.parentElement.parentElement;
	const visibleId: HTMLTableCellElement = vocTr.getElementsByClassName('ts-visible-id')[0] as HTMLTableCellElement;

	// TODO: Change
	// requestVocDelete(setId, visibleId.innerText);

	window.location.reload();
}