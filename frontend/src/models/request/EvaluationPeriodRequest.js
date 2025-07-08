export class EvaluationPeriodRequest {
    constructor(name, startDate, endDate, templateIds) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.templateIds = templateIds;
    }
}
