import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { Toaster } from 'sonner';

import MainLayout from './layouts/MainLayout';

import ProtectedRoute from './components/ProtectedRoute';

import SignInPage from './pages/SignInPage.jsx';
import CompanySignUpPage from './pages/CompanySignUpPage.jsx';
import EmployeeInvitePage from './pages/EmployeeInvitePage.jsx';
import AdminDashboard from './pages/Admin/AdminDashboard.jsx';
import EmployeeDashboard from './pages/Employee/EmployeeDashboard.jsx';
import UnauthorizedPage from './pages/UnauthorizedPage.jsx';
import EmployeeManagementPage from './pages/Admin/EmployeeManagementPage.jsx';
import RoleManagementPage from './pages/Admin/RoleManagementPage.jsx';
import EvaluationPeriodManagementPage from './pages/Admin/EvaluationPeriodManagementPage.jsx';
import EvaluationTemplateManagementPage from './pages/Admin/EvaluationTemplateManagementPage.jsx';
import EmployeeEvaluationsPage from './pages/Admin/EmployeeEvaluationsPage.jsx';
import AdminEvaluationDetailsPage from './pages/Admin/AdminEvaluationDetailsPage.jsx';
import EvaluationTasksPage from './pages/Employee/EvaluationTasksPage.jsx';
import EvaluationPage from './pages/Employee/EvaluationPage.jsx';

import './App.css';

function App() {
    const { user } = useSelector((state) => state.auth);

    const getDefaultRoute = () => {
        if (!user) return '/sign-in';
        const role = user.roles[0];
        if (role === 'ADMIN' || role === 'COMPANY') {
            return '/admin/dashboard';
        }
        if (role === 'EMPLOYEE') {
            return '/employee/dashboard';
        }
        return '/sign-in';
    };

    return (
        <Router>
            <Toaster richColors position="top-right" />
            <Routes>
                {/* Public Routes */}
                <Route path="/sign-in" element={<SignInPage />} />
                <Route path="/company-sign-up" element={<CompanySignUpPage />} />
                <Route path="/complete-invitation/:token" element={<EmployeeInvitePage />} />
                <Route path="/unauthorized" element={<UnauthorizedPage />} />

                {/* Protected Routes */}
                <Route element={<MainLayout />}>
                    <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'COMPANY']} />}>
                        <Route path="/admin/dashboard" element={<AdminDashboard />} />
                        <Route path="/admin/employees" element={<EmployeeManagementPage />} />
                        <Route path="/admin/roles" element={<RoleManagementPage />} />
                        <Route path="/admin/periods" element={<EvaluationPeriodManagementPage />} />
                        <Route path="/admin/templates" element={<EvaluationTemplateManagementPage />} />
                        <Route path="/admin/employee/:id/evaluations" element={<EmployeeEvaluationsPage />} />
                        <Route path="/admin/evaluation/:evaluationId" element={<AdminEvaluationDetailsPage />} />
                    </Route>
                    <Route element={<ProtectedRoute allowedRoles={['EMPLOYEE']} />}>
                        <Route path="/employee/dashboard" element={<EmployeeDashboard />} />
                        <Route path="/employee/period/:periodId/tasks" element={<EvaluationTasksPage />} />
                        <Route path="/employee/evaluation/:evaluationId" element={<EvaluationPage />} />
                    </Route>
                </Route>

                {/* Redirects */}
                <Route path="/" element={<Navigate to={getDefaultRoute()} replace />} />
                <Route path="*" element={<Navigate to={getDefaultRoute()} replace />} />
            </Routes>
        </Router>
    );
}

export default App;