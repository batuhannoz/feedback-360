import apiClient from './api';

const getMyEvaluationPeriods = () => {
    return apiClient.get('/me/period');
};

const getMyEvaluationsForPeriod = (periodId) => {
    return apiClient.get(`/me/period/${periodId}/evaluation`);
};

const getQuestionsForEvaluation = (periodId, evaluatedUserId) => {
    return apiClient.get(`/me/period/${periodId}/evaluation/${evaluatedUserId}`);
};

const submitAnswers = (periodId, evaluatedUserId, request) => {
    return apiClient.post(`/me/period/${periodId}/evaluation/${evaluatedUserId}`, request);
};

const UserEvaluationService = {
    getMyEvaluationPeriods,
    getMyEvaluationsForPeriod,
    getQuestionsForEvaluation,
    submitAnswers
};

export default UserEvaluationService;
