import React from 'react';
import { useSelector } from 'react-redux';
import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoute = ({ allowedRoles }) => {
    const { role, accessToken } = useSelector((state) => state.auth);

    if (!accessToken) {
        return <Navigate to="/sign-in" replace />;
    }

    return allowedRoles.includes(role) ? <Outlet /> : <Navigate to="/unauthorized" replace />;
};

export default ProtectedRoute;
