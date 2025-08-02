import React from 'react';
import { Outlet } from 'react-router-dom';
import EmployeeHeader from './EmployeeHeader.jsx';

const EmployeeLayout = () => {
    return (
        <div className="min-h-screen bg-gray-100">
            <EmployeeHeader />
            <main className="p-4">
                <Outlet />
            </main>
        </div>
    );
};

export default EmployeeLayout;
