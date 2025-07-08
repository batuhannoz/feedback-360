import { QuestionType } from '../enums/QuestionType.js';

export class QuestionRequest {
    constructor(question, type) {
        this.question = question;
        this.type = type;
    }
}
