import apiClient from './api';

const getParticipantStatus = (periodId) => {
    return apiClient.get(`/evaluation/periods/${periodId}/participants`);
};

const getReceivedEvaluations = (periodId, userId) => {
    return apiClient.get(`/evaluation/periods/${periodId}/user/${userId}/received`);
};

const getGivenEvaluations = (periodId, userId) => {
    return apiClient.get(`/evaluation/periods/${periodId}/user/${userId}/given`);
};

const getEvaluationDetailsForAdmin = (evaluationId) => {
    return apiClient.get(`/evaluation/${evaluationId}/details`);
};

const EvaluationService = {
    getParticipantStatus,
    getReceivedEvaluations,
    getGivenEvaluations,
    getEvaluationDetailsForAdmin,
};

export default EvaluationService;
