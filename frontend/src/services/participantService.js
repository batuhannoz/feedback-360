import apiClient from './api';

export const getParticipantsByPeriod = (periodId) => {
    return apiClient.get(`/period/${periodId}/participant`);
};

export const addParticipantToPeriod = (periodId, userId) => {
    return apiClient.post(`/period/${periodId}/participant/${userId}`);
};

export const deleteParticipantFromPeriod = (periodId, userId) => {
    return apiClient.delete(`/period/${periodId}/participant/${userId}`);
};

export const getParticipantAssignments = (periodId, participantId) => {
    return apiClient.get(`/period/${periodId}/participant/${participantId}/assignment`);
};

export const saveParticipantAssignments = (periodId, participantId, assignments) => {
    return apiClient.post(`/period/${periodId}/participant/${participantId}/assignment`, assignments);
};
