import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar';

const MainLayout = () => {
    return (
        <div className="flex h-screen bg-gray-100">
            <Sidebar />
            <div className="flex-1 p-10 overflow-y-auto">
                <Outlet />
            </div>
        </div>
    );
};

export default MainLayout;
