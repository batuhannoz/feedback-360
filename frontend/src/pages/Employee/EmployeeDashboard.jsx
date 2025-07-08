import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import EmployeeService from '../../services/employeeService';

const EmployeeDashboard = () => {
    const [periods, setPeriods] = useState([]);

    useEffect(() => {
        fetchPeriods();
    }, []);

    const fetchPeriods = async () => {
        try {
            const response = await EmployeeService.getEmployeePeriods();
            setPeriods(response.data);
        } catch (error) {
            console.error('Error fetching periods:', error);
        }
    };

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">My Evaluation Periods</h1>
            <div className="bg-white shadow-md rounded-lg">
                <table className="min-w-full leading-normal">
                    <thead>
                        <tr>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Period Name</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Start Date</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">End Date</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {periods.map((period) => (
                            <tr key={period.id}>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{period.name}</td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{new Date(period.startDate).toLocaleDateString()}</td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{new Date(period.endDate).toLocaleDateString()}</td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{period.status}</td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <Link to={`/employee/period/${period.id}/tasks`} className="text-indigo-600 hover:text-indigo-900">
                                        View Tasks
                                    </Link>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default EmployeeDashboard;
