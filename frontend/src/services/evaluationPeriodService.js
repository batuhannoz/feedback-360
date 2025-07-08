import apiClient from './api';
import { EvaluationPeriodRequest } from '../models/request/EvaluationPeriodRequest.js';

const createEvaluationPeriod = (evaluationPeriodRequest) => {
    return apiClient.post('/evaluation/period', evaluationPeriodRequest);
};

const getAllPeriods = () => {
    return apiClient.get('/evaluation/period');
};

const getPeriodById = (periodId) => {
    return apiClient.get(`/evaluation/period/${periodId}`);
};

const updatePeriod = (periodId, evaluationPeriodRequest) => {
    return apiClient.put(`/evaluation/period/${periodId}`, evaluationPeriodRequest);
};

const deletePeriod = (periodId) => {
    return apiClient.delete(`/evaluation/period/${periodId}`);
};

const EvaluationPeriodService = {
    createEvaluationPeriod,
    getAllPeriods,
    getPeriodById,
    updatePeriod,
    deletePeriod,
};

export default EvaluationPeriodService;
