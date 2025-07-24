import apiClient from './api';

const getQuestionsForCompetency = (periodId, competencyId) => {
    return apiClient.get(`/period/${periodId}/competency/${competencyId}/question`);
};

const addQuestionToCompetency = (periodId, competencyId, questionRequest) => {
    return apiClient.post(`/period/${periodId}/competency/${competencyId}/question`, questionRequest);
};

const updateQuestion = (periodId, competencyId, questionId, questionRequest) => {
    return apiClient.put(`/period/${periodId}/competency/${competencyId}/question/${questionId}`, questionRequest);
};

const deleteQuestion = (periodId, competencyId, questionId) => {
    return apiClient.delete(`/period/${periodId}/competency/${competencyId}/question/${questionId}`);
};

const CompetencyQuestionService = {
    getQuestionsForCompetency,
    addQuestionToCompetency,
    updateQuestion,
    deleteQuestion
};

export default CompetencyQuestionService;