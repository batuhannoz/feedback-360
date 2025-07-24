import apiClient from './api';

export const getUsers = (params) => {
    return apiClient.get('/user', { params });
};

export const createUser = (userRequest) => {
    return apiClient.post('/user', userRequest);
};

export const updateUser = (userId, userRequest) => {
    return apiClient.put(`/user/${userId}`, userRequest);
};

export const getUserDetails = (userId) => {
    return apiClient.get(`/user/${userId}`);
};

const getUserAssignments = (periodId, userId) => {
    return apiClient.get(`/user/period/${periodId}/user/${userId}/assignment`);
};

const getAnswersForAssignment = (assignmentId) => {
    return apiClient.get(`/user/assignment/${assignmentId}/answer`);
};

const UserService = {
    createUser,
    updateUser,
    getUsers,
    getUserDetails,
    getUserAssignments,
    getAnswersForAssignment
};

export default UserService;
