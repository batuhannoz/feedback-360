import axios from 'axios';
import {showErrorToast, showSuccessToast} from './notification';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

let store;
export const injectStore = (_store) => {
    store = _store;
};

apiClient.interceptors.request.use(
    (config) => {
        if (store) {
            const token = store.getState().auth.accessToken;
            if (token) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

apiClient.interceptors.response.use(
    (response) => {
        if (response.data && typeof response.data.success === 'boolean') {
            if (response.data.success) {
                if (response.data.message) {
                    showSuccessToast(response.data.message);
                }
                return response.data;
            } else {
                showErrorToast(response.data.message || 'An unexpected error occurred.');
                return Promise.reject(new Error(response.data.message || 'An unexpected error occurred.'));
            }
        }
        return response;
    },
    (error) => {
        const message = error.response?.data?.message || error.message || 'A network error occurred.';
        showErrorToast(message);
        return Promise.reject(error);
    }
);

export default apiClient;
