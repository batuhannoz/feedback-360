import apiClient from './api';
import { EmployeeRequest } from '../models/request/EmployeeRequest.js';
import { SubmitAnswersRequest } from '../models/request/SubmitAnswersRequest.js';

const createEmployee = (employeeRequest) => {
    return apiClient.put('/employee', employeeRequest);
};

const updateEmployee = (id, employeeRequest) => {
    return apiClient.put(`/employee/${id}`, employeeRequest);
};

const listEmployees = () => {
    return apiClient.get('/employee');
};

const getEmployeeDetails = (id) => {
    return apiClient.get(`/employee/${id}`);
};

const assignRoleToEmployee = (employeeId, roleId) => {
    return apiClient.post(`/employee/${employeeId}/role/${roleId}`);
};

const removeRoleFromEmployee = (employeeId, roleId) => {
    return apiClient.delete(`/employee/${employeeId}/role/${roleId}`);
};

const getEmployeePeriods = () => {
    return apiClient.get('/employee/period');
};

const getEvaluationTasksForPeriod = (periodId) => {
    return apiClient.get(`/employee/period/${periodId}/tasks`);
};

const getEvaluationDetails = (evaluationId) => {
    return apiClient.get(`/employee/evaluation/${evaluationId}`);
};

const submitAnswers = (evaluationId, submitAnswersRequest) => {
    return apiClient.put(`/employee/evaluation/${evaluationId}/answers`, submitAnswersRequest);
};

const deleteEmployee = (id) => {
    return apiClient.delete(`/employee/${id}`);
};

const EmployeeService = {
    createEmployee,
    updateEmployee,
    listEmployees,
    getEmployeeDetails,
    assignRoleToEmployee,
    deleteEmployee,
    removeRoleFromEmployee,
    getEmployeePeriods,
    getEvaluationTasksForPeriod,
    getEvaluationDetails,
    submitAnswers,
};

export default EmployeeService;
