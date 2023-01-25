

export function debug(...data: any[]) {
	if (localStorage.getItem('debug-key') !== null) {
		console.log(...data);
	}
}