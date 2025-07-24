import apiClient from './api';

const getUserPeriodReport = (periodId, evaluatedUserId) => {
    return apiClient.get(`/period/${periodId}/report/user/${evaluatedUserId}`);
};

const shareUserPeriodReport = (periodId, evaluatedUserId, request) => {
    return apiClient.post(`/period/${periodId}/report/user/${evaluatedUserId}/share`, request);
};

const ReportService = {
    getUserPeriodReport,
    shareUserPeriodReport
};

export default ReportService;
