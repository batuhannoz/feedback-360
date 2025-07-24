import apiClient from './api';

const createPeriod = (evaluationPeriodRequest) => {
    return apiClient.post('/period', evaluationPeriodRequest);
};

const getPeriods = () => {
    return apiClient.get('/period');
};

const getPeriodById = (periodId) => {
    return apiClient.get(`/period/${periodId}`);
};

const updatePeriod = (periodId, evaluationPeriodRequest) => {
    return apiClient.put(`/period/${periodId}`, evaluationPeriodRequest);
};

const getCompetencyWeights = (periodId) => {
    return apiClient.get(`/period/${periodId}/competency/weight`);
};

const setCompetencyWeights = (periodId, data) => {
    return apiClient.post(`/period/${periodId}/competency/weight`, data);
};

const updatePeriodStatus = (periodId, updatePeriodStatusRequest) => {
    return apiClient.post(`/period/${periodId}/status`, updatePeriodStatusRequest);
};

const deletePeriod = (periodId) => {
    return apiClient.delete(`/period/${periodId}`);
};

export default {
    createPeriod,
    getPeriods,
    getPeriodById,
    updatePeriod,
    updatePeriodStatus,
    deletePeriod,
    getCompetencyWeights,
    setCompetencyWeights
};
