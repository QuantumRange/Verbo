import {Voc} from "./Vocabulary.js";

export class VocSet {
	id: bigint;
	name: string;
	owner: bigint;

	vocabularies: Voc[];


	constructor(id: bigint, name: string, owner: bigint, vocabularies: Voc[]) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.vocabularies = vocabularies;
	}

}