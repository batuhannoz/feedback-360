import apiClient from './api';

export const getEvaluatorsByPeriodId = (periodId) => {
    return apiClient.get(`/period/${periodId}/evaluator`);
};

export const setEvaluatorsByPeriodId = (periodId, evaluators) => {
    return apiClient.put(`/period/${periodId}/evaluator`, evaluators);
};

