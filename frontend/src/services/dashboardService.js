import api from './api';

const getDashboardStats = () => {
    return api.get('/dashboard/stats');
};

const DashboardService = {
    getDashboardStats,
};

export default DashboardService;
