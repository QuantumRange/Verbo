export type EventHandler = (element: HTMLElement | Element) => void;

export function attachEvent(elementClass: string, type: string, onEvent: EventHandler): void {
	const elements: HTMLCollectionOf<Element> = document.getElementsByClassName(elementClass);

	for (let i = 0; i < elements.length; i++) {
		const element: Element = elements[i];

		element.addEventListener(type, _ => onEvent(element));
	}
}

export function shuffle<T>(array: T[]): T[] {
	let result: T[] = [];

	for (let i = 0; i < array.length; i++) {
		result[i] = array[i];
	}

	let currentIndex = array.length, randomIndex;

	while (currentIndex != 0) {
		randomIndex = Math.floor(Math.random() * currentIndex);
		currentIndex--;

		[result[currentIndex], result[randomIndex]] = [result[randomIndex], result[currentIndex]];
	}

	return result;
}

export function setProgress(progressBar: HTMLDivElement, progress: number) {
	progressBar.style.width = (progress * 100) + '%';
}