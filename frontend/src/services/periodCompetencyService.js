import apiClient from './api';

const addCompetencyToPeriod = (periodId, competencyRequest) => {
    return apiClient.post(`/period/${periodId}/competency`, competencyRequest);
};

const getCompetenciesByPeriod = (periodId) => {
    return apiClient.get(`/period/${periodId}/competency`);
};

const getCompetencyById = (periodId, competencyId) => {
    return apiClient.get(`/period/${periodId}/competency/${competencyId}`);
};

const deleteCompetencyFromPeriod = (periodId, competencyId) => {
    return apiClient.delete(`/period/${periodId}/competency/${competencyId}`);
};

const assignEvaluatorsToCompetency = (periodId, competencyId, request) => {
    return apiClient.post(`/period/${periodId}/competency/${competencyId}/evaluator`, request);
};

const getCompetencyEvaluatorPermissions = (periodId, competencyId) => {
    return apiClient.get(`/period/${periodId}/competency/${competencyId}/evaluator`);
};

const setCompetencyEvaluatorWeights = (periodId, competencyId, request) => {
    return apiClient.post(`/period/${periodId}/competency/${competencyId}/evaluator/weight`, request);
};

const getCompetencyWeights = (periodId) => {
    return apiClient.get(`/period/${periodId}/competency/weight`);
};

const setCompetencyWeights = (periodId, request) => {
    return apiClient.post(`/period/${periodId}/competency/weight`, request);
};

const PeriodCompetencyService = {
    addCompetencyToPeriod,
    getCompetenciesByPeriod,
    getCompetencyById,
    deleteCompetencyFromPeriod,
    assignEvaluatorsToCompetency,
    getCompetencyEvaluatorPermissions,
    setCompetencyEvaluatorWeights,
    getCompetencyWeights,
    setCompetencyWeights
};

export default PeriodCompetencyService;
