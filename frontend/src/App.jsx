import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { Toaster } from 'sonner';

import LoginPage from './pages/auth/LoginPage.jsx';
import CompanySignUpPage from './pages/auth/CompanySignUpPage.jsx';
import AcceptInvitationPage from './pages/auth/AcceptInvitationPage.jsx';
import UnauthorizedPage from './pages/UnauthorizedPage.jsx';

import AdminLayout from './components/layout/admin/AdminLayout.jsx';
import EmployeeLayout from './components/layout/employee/EmployeeLayout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

import DashboardPage from './pages/admin/DashboardPage.jsx';
import EmployeesPage from './pages/admin/EmployeesPage.jsx';
import EmployeeDetailPage from './pages/admin/EmployeeDetailPage.jsx';
import AssignmentAnswersPage from './pages/admin/AssignmentAnswersPage.jsx';
import StartPeriodPage from './pages/admin/StartPeriodPage.jsx';
import MyPeriodsPage from './pages/user/MyPeriodsPage.jsx';
import MyAssignmentsPage from './pages/user/MyAssignmentsPage.jsx';
import EvaluationPage from './pages/user/EvaluationPage.jsx';
import SettingsPage from './pages/admin/SettingsPage.jsx';
import EvaluationsPage from './pages/admin/EvaluationsPage.jsx';
import ParticipantsPage from './pages/admin/ParticipantsPage.jsx';
import CompetenciesPage from './pages/admin/CompetenciesPage.jsx';
import TemplatesPage from './pages/admin/TemplatesPage.jsx';
import WeightsPage from './pages/admin/CompetencyWeightsPage.jsx';
import EvaluatorsPage from "./pages/admin/EvaluatorsPage.jsx";
import EvaluationPeriodDetailPage from './pages/admin/EvaluationPeriodDetailPage.jsx';

import './App.css';
import SourceWeightsPage from "./pages/admin/SourceWeightsPage.jsx";
import EmployeeReportPage from "./pages/admin/EmployeeReportPage.jsx";

function App() {
    const { user, role } = useSelector((state) => state.auth);

    const getDefaultRoute = () => {
        if (!user) return '/sign-in';
        console.log(role)
        if (role === 'ROLE_ADMIN') return '/dashboard';
        if (role === 'ROLE_EMPLOYEE') return '/my-evaluations';
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

                <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_COMPANY_OWNER', 'ROLE_EMPLOYEE']} />}>
                    {/* User-facing routes */}
                    <Route element={<EmployeeLayout />}>
                        <Route path="/my-evaluations" element={<MyPeriodsPage />} />
                        <Route path="/my-evaluations/:periodId/assignments" element={<MyAssignmentsPage />} />
                        <Route path="/my-evaluations/:periodId/assignments/:evaluatedUserId" element={<EvaluationPage />} />
                    </Route>

                    {/* Admin-only routes */}
                    <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_COMPANY_OWNER']} />}>
                        <Route path="/dashboard/*" element={<AdminLayout />}>
                            <Route index element={<DashboardPage />} />
                            <Route path="employees" element={<EmployeesPage />} />
                            <Route path="employees/:userId" element={<EmployeeDetailPage />} />
                            <Route path="employees/:userId/report" element={<EmployeeReportPage />} />
                            <Route path="assignments/:assignmentId/answers" element={<AssignmentAnswersPage />} />
                            <Route path="start-period" element={<StartPeriodPage />} />
                            <Route path="evaluators" element={<EvaluatorsPage/>} />
                            <Route path="settings" element={<SettingsPage />} />
                            <Route path="evaluations" element={<EvaluationsPage />} />
                            <Route path="evaluations/:periodId" element={<EvaluationPeriodDetailPage />} />
                            <Route path="participants" element={<ParticipantsPage />} />
                            <Route path="competencies" element={<CompetenciesPage />} />
                            <Route path="templates" element={<TemplatesPage />} />
                            <Route path="competency-weights" element={<WeightsPage />} />
                            <Route path="source-weights" element={<SourceWeightsPage />} />
                        </Route>
                    </Route>
                </Route>

                <Route path="/" element={<Navigate to={getDefaultRoute()} replace />} />
                <Route path="*" element={<Navigate to={getDefaultRoute()} replace />} />
            </Routes>
        </Router>
    );
}

export default App;