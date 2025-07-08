import { AnswerPayload } from './AnswerPayload.js';

export class SubmitAnswersRequest {
    constructor(answers) {
        this.answers = answers.map(answer => new AnswerPayload(answer.answerId, answer.value));
    }
}
