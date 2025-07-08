import apiClient from './api';
import { RoleRequest } from '../models/request/RoleRequest.js';

const createRole = (roleRequest) => {
    return apiClient.post('/role', roleRequest);
};

const getEmployeesForRole = (roleId) => {
    return apiClient.get(`/role/${roleId}/employee`);
};

const getAllRoles = () => {
    return apiClient.get('/role');
};

const deleteRole = (roleId) => {
    return apiClient.delete(`/role/${roleId}`);
};

const RoleService = {
    createRole,
    getEmployeesForRole,
    getAllRoles,
    deleteRole,
};

export default RoleService;
