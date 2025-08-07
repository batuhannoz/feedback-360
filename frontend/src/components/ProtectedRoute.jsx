import React, {useEffect} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import { Navigate, Outlet } from 'react-router-dom';
import {fetchAndStoreLogoUrl} from "../store/authSlice.js";

const ProtectedRoute = ({ allowedRoles }) => {
    const dispatch = useDispatch();
    const { role, accessToken, isAuthenticated } = useSelector((state) => state.auth);

    useEffect(() => {
        if (isAuthenticated) {
            dispatch(fetchAndStoreLogoUrl());
        }
    }, [dispatch, isAuthenticated]);

    if (!accessToken) {
        return <Navigate to="/sign-in" replace />;
    }

    return allowedRoles.includes(role) ? <Outlet /> : <Navigate to="/unauthorized" replace />;
};

export default ProtectedRoute;
