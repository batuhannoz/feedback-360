import api from './api';

const EvaluationScaleService = {
    getScales: () => {
        return api.get('/scales');
    },

    createScale: (scaleData) => {
        return api.post('/scales', scaleData);
    },

    updateScale: (scaleId, scaleData) => {
        return api.put(`/scales/${scaleId}`, scaleData);
    },

    deleteScale: (scaleId) => {
        return api.delete(`/scales/${scaleId}`);
    },
};

export default EvaluationScaleService;