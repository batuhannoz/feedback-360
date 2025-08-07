import api from './api';

const API_URL = '/company/settings';

export const getCompanySettings = () => {
    return api.get(API_URL);
};

export const updateCompanyInfo = (companyInfo) => {
    return api.put(`${API_URL}`, companyInfo);
};

export const updateCompanyLogo = (file) => {
    const formData = new FormData();
    formData.append('file', file);

    return api.post(`${API_URL}/logo`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
};

export const getLogoUrl = () => {
    const t = new Date().getTime();
    return api.get(API_URL + '/logo?v=' + t, {
        responseType: 'blob'
    });
};

const CompanyService = {
    getCompanySettings,
    updateCompanyInfo,
    updateCompanyLogo,
    getLogoUrl
}

export default CompanyService;