import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import EvaluationService from '../../services/evaluationService';
import EvaluationPeriodService from '../../services/evaluationPeriodService';
import EmployeeService from '../../services/employeeService';

const EmployeeEvaluationsPage = () => {
    const { id } = useParams();
    const [employee, setEmployee] = useState(null);
    const [periods, setPeriods] = useState([]);
    const [selectedPeriod, setSelectedPeriod] = useState('');
    const [receivedEvaluations, setReceivedEvaluations] = useState([]);
    const [givenEvaluations, setGivenEvaluations] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchEmployeeDetails = async () => {
            try {
                const res = await EmployeeService.getEmployeeDetails(id);
                setEmployee(res.data);
            } catch (error) {
                console.error('Error fetching employee details:', error);
            }
        };

        const fetchPeriods = async () => {
            try {
                const res = await EvaluationPeriodService.getAllPeriods();
                setPeriods(res.data);
                if (res.data.length > 0) {
                    setSelectedPeriod(res.data[0].id);
                }
            } catch (error) {
                console.error('Error fetching evaluation periods:', error);
            }
        };

        fetchEmployeeDetails();
        fetchPeriods();
    }, [id]);

    useEffect(() => {
        if (selectedPeriod && id) {
            fetchEvaluations();
        }
    }, [selectedPeriod, id]);

    const fetchEvaluations = async () => {
        setLoading(true);
        try {
            const [receivedRes, givenRes] = await Promise.all([
                EvaluationService.getReceivedEvaluations(selectedPeriod, id),
                EvaluationService.getGivenEvaluations(selectedPeriod, id)
            ]);
            setReceivedEvaluations(receivedRes.data);
            setGivenEvaluations(givenRes.data);
        } catch (error) {
            console.error('Error fetching evaluations:', error);
            setReceivedEvaluations([]);
            setGivenEvaluations([]);
        }
        setLoading(false);
    };

    const renderEvaluationList = (title, evaluations) => (
        <div className="mb-8">
            <h2 className="text-xl font-semibold mb-4">{title}</h2>
            {evaluations.length > 0 ? (
                <ul className="space-y-4">
                    {evaluations.map(evaluation => (
                        <li key={evaluation.evaluationId}>
                            <Link to={`/admin/evaluation/${evaluation.evaluationId}`} className="block p-4 bg-white rounded-lg shadow hover:bg-gray-100 transition-colors">
                                <div className="flex justify-between items-center">
                                    <div>
                                        <p><strong>From:</strong> {evaluation.evaluator.fullName}</p>
                                        <p><strong>To:</strong> {evaluation.evaluated.fullName}</p>
                                        <p><strong>Status:</strong> {evaluation.status}</p>
                                    </div>
                                    <span className="text-indigo-600 hover:text-indigo-900">View Details</span>
                                </div>
                            </Link>
                        </li>
                    ))}
                </ul>
            ) : (
                <p>No evaluations found.</p>
            )}
        </div>
    );

    if (!employee) {
        return <div>Loading employee details...</div>;
    }

    return (
        <div className="container mx-auto p-6">
            <h1 className="text-3xl font-bold mb-6">Evaluations for {employee.firstName} {employee.lastName}</h1>

            <div className="mb-6">
                <label htmlFor="period-select" className="block text-sm font-medium text-gray-700">Select Evaluation Period:</label>
                <select
                    id="period-select"
                    value={selectedPeriod}
                    onChange={(e) => setSelectedPeriod(e.target.value)}
                    className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
                >
                    {periods.map(period => (
                        <option key={period.id} value={period.id}>{period.name}</option>
                    ))}
                </select>
            </div>

            {loading ? (
                <p>Loading evaluations...</p>
            ) : (
                <div>
                    {renderEvaluationList('Received Evaluations', receivedEvaluations)}
                    {renderEvaluationList('Given Evaluations', givenEvaluations)}
                </div>
            )}
        </div>
    );
};

export default EmployeeEvaluationsPage;
