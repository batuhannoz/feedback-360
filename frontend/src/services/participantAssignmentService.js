import apiClient from './api';

const getEvaluatorsForPeriodAssignment = (periodId, evaluatedUserId) => {
    return apiClient.get(`/period/${periodId}/participant/${evaluatedUserId}/assignment`);
};

const assignEvaluatorsToPeriodParticipant = (periodId, evaluatedUserId, request) => {
    return apiClient.post(`/period/${periodId}/participant/${evaluatedUserId}/assignment`, request);
};

const ParticipantAssignmentService = {
    getEvaluatorsForPeriodAssignment,
    assignEvaluatorsToPeriodParticipant
};

export default ParticipantAssignmentService;
