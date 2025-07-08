import apiClient from './api';
import { EvaluationTemplateRequest } from '../models/request/EvaluationTemplateRequest.js';
import { QuestionRequest } from '../models/request/QuestionRequest.js';
import { SetTemplateVisibilityRequest } from '../models/request/SetTemplateVisibilityRequest.js';

const getAllTemplates = () => {
    return apiClient.get('/evaluation/template');
};

const createTemplate = (evaluationTemplateRequest) => {
    return apiClient.post('/evaluation/template', evaluationTemplateRequest);
};

const addQuestionToTemplate = (templateId, questionRequest) => {
    return apiClient.post(`/evaluation/template/${templateId}/question`, questionRequest);
};

const removeQuestionFromTemplate = (templateId, questionId) => {
    return apiClient.delete(`/evaluation/template/${templateId}/question/${questionId}`);
};

const setTemplateVisibility = (templateId, setTemplateVisibilityRequest) => {
    return apiClient.post(`/evaluation/template/${templateId}/visibility`, setTemplateVisibilityRequest);
};

const updateTemplate = (templateId, evaluationTemplateRequest) => {
    return apiClient.put(`/evaluation/template/${templateId}`, evaluationTemplateRequest);
};

const deleteTemplate = (templateId) => {
    return apiClient.delete(`/evaluation/template/${templateId}`);
};

const EvaluationTemplateService = {
    getAllTemplates,
    createTemplate,
    addQuestionToTemplate,
    removeQuestionFromTemplate,
    setTemplateVisibility,
    updateTemplate,
    deleteTemplate,
};

export default EvaluationTemplateService;
