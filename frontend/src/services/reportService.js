import apiClient from './api';

const getUserPeriodReport = (periodId, evaluatedUserId, settings) => {
    return apiClient.post(`/period/${periodId}/report/user/${evaluatedUserId}`, settings);
};

const ReportService = {
    getUserPeriodReport
};

export default ReportService;
