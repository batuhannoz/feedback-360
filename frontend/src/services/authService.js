import apiClient from './api';

const companySignUp = (signUpRequest) =>  {
    return apiClient.post('/auth/company/sign-up', signUpRequest);
};

const completeInvitation = (userSignUpRequest) => {
    return apiClient.post('/auth/invitation', userSignUpRequest);
};

const signIn = (signInRequest) => {
    return apiClient.post('/auth/sign-in', signInRequest);
};

const refreshToken = (refreshTokenRequest) => {
    return apiClient.post('/auth/refresh', refreshTokenRequest);
};

const forgotPassword = (forgotPasswordRequest) => {
    return apiClient.post('/auth/forgot-password', forgotPasswordRequest);
};

const resetPassword = (resetPasswordRequest) => {
    return apiClient.post('/auth/reset-password', resetPasswordRequest);
};

const AuthService = {
    companySignUp,
    signIn,
    completeInvitation,
    refreshToken,
    forgotPassword,
    resetPassword
};

export default AuthService;