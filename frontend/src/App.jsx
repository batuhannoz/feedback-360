import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { Toaster } from 'sonner';

import LoginPage from './pages/auth/LoginPage.jsx';
import CompanySignUpPage from './pages/auth/CompanySignUpPage.jsx';
import AcceptInvitationPage from './pages/auth/AcceptInvitationPage.jsx';
import UnauthorizedPage from './pages/UnauthorizedPage.jsx';

import AdminLayout from './components/layout/AdminLayout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

import DashboardPage from './pages/admin/DashboardPage.jsx';
import EmployeesPage from './pages/admin/EmployeesPage.jsx';
import ReportsPage from './pages/admin/ReportsPage.jsx';
import SettingsPage from './pages/admin/SettingsPage.jsx';
import EvaluationsPage from './pages/admin/EvaluationsPage.jsx';
import ParticipantsPage from './pages/admin/ParticipantsPage.jsx';
import CompetenciesPage from './pages/admin/CompetenciesPage.jsx';
import TemplatesPage from './pages/admin/TemplatesPage.jsx';
import WeightsPage from './pages/admin/WeightsPage.jsx';
import EvaluatorsPage from "./pages/admin/EvaluatorsPage.jsx";

import './App.css';

function App() {
    const { user } = useSelector((state) => state.auth);

    const getDefaultRoute = () => {
        if (!user) return '/sign-in';
        return '/dashboard'
    };

    return (
        <Router>
            <Toaster richColors position="bottom-right" duration={1500} />
            <Routes>
                <Route path="/sign-in" element={<LoginPage />} />
                <Route path="/company/sign-up" element={<CompanySignUpPage />} />
                <Route path="/invitation" element={<AcceptInvitationPage />} />
                <Route path="/forgot-password" element={<Navigate to="/sign-in" replace />} />
                <Route path="/unauthorized" element={<UnauthorizedPage />} />

                <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_COMPANY_OWNER']} />}>
                    <Route path="/dashboard" element={<AdminLayout />}>
                        <Route index element={<DashboardPage />} />
                        <Route path="employees" element={<EmployeesPage />} />
                        <Route path="evaluators" element={<EvaluatorsPage/>} />
                        <Route path="reports" element={<ReportsPage />} />
                        <Route path="settings" element={<SettingsPage />} />
                        <Route path="evaluations" element={<EvaluationsPage />} />
                        <Route path="participants" element={<ParticipantsPage />} />
                        <Route path="competencies" element={<CompetenciesPage />} />
                        <Route path="templates" element={<TemplatesPage />} />
                        <Route path="evaluations/:periodId/weights" element={<WeightsPage />} />
                    </Route>
                </Route>

                <Route path="/" element={<Navigate to={getDefaultRoute()} replace />} />
                <Route path="*" element={<Navigate to={getDefaultRoute()} replace />} />
            </Routes>
        </Router>
    );
}

export default App;