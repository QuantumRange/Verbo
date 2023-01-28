
export type ButtonEvent = () => void;

export type PopupButton = {
	title: string,
	class: string | null,
	action: ButtonEvent | null
};

export function popup(title: string,
                             body: string,
                             options: PopupButton[]): void {
	let modal = document.createElement('div');
	modal.classList.add('modal', 'fade');
	modal.tabIndex = -1;
	modal.role = 'dialog';
	modal.ariaHidden = 'true';

	let modalDialog = document.createElement('div');
	modalDialog.classList.add('modal-dialog');
	modal.appendChild(modalDialog);

	let modalContent = document.createElement('div');
	modalContent.classList.add('modal-content');
	modalDialog.appendChild(modalContent);

	let modalHeader = document.createElement('div');
	modalHeader.classList.add('modal-header');
	modalContent.appendChild(modalHeader);

	let modalTitle = document.createElement('h5');
	modalTitle.classList.add('modal-title');
	modalTitle.innerHTML = title;
	modalHeader.appendChild(modalTitle);

	let modalBody = document.createElement('div');
	modalBody.classList.add('modal-body');
	modalBody.innerHTML = body;
	modalContent.appendChild(modalBody);

	let modalFooter = document.createElement('div');
	modalFooter.classList.add('modal-footer');
	modalContent.appendChild(modalFooter);

	options.forEach(option => {
		let btn = document.createElement('button');
		btn.classList.add('btn', option.class === null ? 'btn-secondary' : option.class);
		btn.innerHTML = option.title;
		btn.addEventListener('click', () => {
			if (option.action !== null)
				option.action();
			modal.remove();
		});
		modalFooter.appendChild(btn);
	});

	document.body.appendChild(modal);
	modal.classList.add('show');
	modal.style.display = "block";
	modal.setAttribute('aria-modal', 'true');
}

export function confirmPopup(title: string, body: string, result: (success: boolean) => void): void {
	popup(title, body, [
		{
			title: 'Cancel',
			class: 'btn-secondary',
			action: () => result(false)
		},
		{
			title: 'Confirm',
			class: 'btn-primary',
			action: () => result(true)
		}
	]);
}