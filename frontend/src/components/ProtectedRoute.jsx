import React from 'react';
import { useSelector } from 'react-redux';
import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoute = ({ allowedRoles }) => {
    const { user, accessToken } = useSelector((state) => state.auth);

    if (!accessToken) {
        return <Navigate to="/sign-in" replace />;
    }

    const userRole = user?.roles?.[0]; // Assuming user has one role

    return allowedRoles.includes(userRole) ? <Outlet /> : <Navigate to="/unauthorized" replace />;
};

export default ProtectedRoute;
