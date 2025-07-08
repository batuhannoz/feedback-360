import apiClient from './api';
import { SignUpRequest } from '../models/request/SignUpRequest.js';
import { SignInRequest } from '../models/request/SignInRequest.js';
import { EmployeeSignUpRequest } from '../models/request/EmployeeSignUpRequest.js';
import { RefreshTokenRequest } from '../models/request/RefreshTokenRequest.js';

const companySignUp = (signUpRequest) => {
    return apiClient.post('/auth/company/sign-up', signUpRequest);
};

const signIn = (signInRequest) => {
    return apiClient.post('/auth/sign-in', signInRequest);
};

const completeEmployeeInvitation = (employeeSignUpRequest) => {
    return apiClient.post('/auth/employee/invitation', employeeSignUpRequest);
};

const refreshToken = (refreshTokenRequest) => {
    return apiClient.post('/auth/refresh', refreshTokenRequest);
};


const AuthService = {
    companySignUp,
    signIn,
    completeEmployeeInvitation,
    refreshToken,
};

export default AuthService;