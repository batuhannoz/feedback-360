import React from 'react';
import { Link } from 'react-router-dom';

const UnauthorizedPage = () => {
    return (
        <div className="flex flex-col items-center justify-center h-screen bg-gray-100">
            <h1 className="text-4xl font-bold text-red-600">Unauthorized</h1>
            <p className="text-lg text-gray-700 mt-4">You do not have permission to view this page.</p>
            <Link to="/sign-in" className="mt-6 px-4 py-2 text-white bg-blue-600 rounded hover:bg-blue-700">
                Go to Sign In
            </Link>
        </div>
    );
};

export default UnauthorizedPage;
