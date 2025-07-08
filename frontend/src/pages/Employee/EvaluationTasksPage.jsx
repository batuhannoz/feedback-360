import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import EmployeeService from '../../services/employeeService';

const EvaluationTasksPage = () => {
    const { periodId } = useParams();
    const [tasks, setTasks] = useState([]);

    useEffect(() => {
        fetchTasks();
    }, [periodId]);

    const fetchTasks = async () => {
        try {
            const response = await EmployeeService.getEvaluationTasksForPeriod(periodId);
            setTasks(response.data);
        } catch (error) {
            console.error('Error fetching tasks:', error);
        }
    };

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Evaluation Tasks</h1>
            <div className="bg-white shadow-md rounded-lg">
                <table className="min-w-full leading-normal">
                    <thead>
                        <tr>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Evaluated Person</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {tasks.map((task) => (
                            <tr key={task.evaluationId}>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{task.evaluatedEmployee.fullName}</td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">{task.status}</td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <Link to={`/employee/evaluation/${task.evaluationId}`} className="text-indigo-600 hover:text-indigo-900">
                                        {task.status === 'PENDING' ? 'Start Evaluation' : 'View Evaluation'}
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

export default EvaluationTasksPage;
