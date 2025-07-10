export class EvaluationPeriodRequest {
    constructor(name, startDate, endDate, templateIds, isSelfEvaluationIncluded) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.templateIds = templateIds;
        this.isSelfEvaluationIncluded = isSelfEvaluationIncluded;
    }
}
