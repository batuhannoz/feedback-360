import { createSlice } from '@reduxjs/toolkit';
import { jwtDecode } from 'jwt-decode';

const initialState = {
    user: null,
    accessToken: null,
    refreshToken: null,
    isAuthenticated: false,
    role: null,
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        loginSuccess(state, action) {
            state.token = action.payload.token;
            state.isAuthenticated = true;
            const decodedToken = jwtDecode(action.payload.accessToken);
            state.user = decodedToken.sub;
            state.accessToken = action.payload.accessToken;
            state.refreshToken = action.payload.refreshToken;
            state.role = decodedToken.role;
            console.log(state.role)
            localStorage.setItem('accessToken', action.payload.accessToken);
        },
        logout(state) {
            state.user = null;
            state.token = null;
            state.isAuthenticated = false;
            state.role = null;
            localStorage.removeItem('accessToken');
        },
    },
});

export const { loginSuccess, logout } = authSlice.actions;

export default authSlice.reducer;