import {createSlice} from '@reduxjs/toolkit';
import {jwtDecode} from 'jwt-decode';
import CompanyService from '../services/companyService';

const initialState = {
    user: null,
    accessToken: null,
    refreshToken: null,
    isAuthenticated: false,
    role: null,
    logoUrl: null,
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        loginSuccess(state, action) {
            state.isAuthenticated = true;
            const decodedToken = jwtDecode(action.payload.accessToken);
            state.user = decodedToken.sub;
            state.accessToken = action.payload.accessToken;
            state.refreshToken = action.payload.refreshToken;
            state.role = decodedToken.role;
            localStorage.setItem('accessToken', action.payload.accessToken);
        },
        logout(state) {
            state.user = null;
            state.accessToken = null;
            state.refreshToken = null;
            state.isAuthenticated = false;
            state.role = null;
            state.logoUrl = null;
            localStorage.removeItem('accessToken');
        },
        setCompanyLogoUrl(state, action) {
            state.logoUrl = action.payload;
        },
    },
});

export const { loginSuccess, logout, setCompanyLogoUrl } = authSlice.actions;


export const fetchAndStoreLogoUrl = () => (dispatch) => {
    let objectUrl = null;

    const fetchLogo = async () => {
        const response = await CompanyService.getLogoUrl();

        if (response.data && response.data.size > 0) {
            objectUrl = URL.createObjectURL(response.data);
            dispatch(setCompanyLogoUrl(objectUrl));
        } else {
            throw new Error('Logo bulunamadı.');
        }
    };

    fetchLogo();

    return () => {
        if (objectUrl) {
            URL.revokeObjectURL(objectUrl);
        }
    };
};


export default authSlice.reducer;