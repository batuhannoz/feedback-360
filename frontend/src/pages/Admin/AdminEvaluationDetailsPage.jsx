import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import EvaluationService from '../../services/evaluationService';
import QuestionRenderer from '../../components/QuestionRenderer';

const AdminEvaluationDetailsPage = () => {
    const { evaluationId } = useParams();
    const [evaluation, setEvaluation] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDetails = async () => {
            try {
                setLoading(true);
                const response = await EvaluationService.getEvaluationDetailsForAdmin(evaluationId);
                setEvaluation(response.data);
            } catch (error) {
                console.error('Error fetching evaluation details:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchDetails();
    }, [evaluationId]);

    if (loading) {
        return <div>Loading evaluation details...</div>;
    }

    if (!evaluation) {
        return <div>Evaluation not found.</div>;
    }

    return (
        <div className="container mx-auto p-6">
            <div className="bg-white shadow-md rounded-lg p-6 mb-6">
                <h1 className="text-2xl font-bold mb-4">Evaluation Details</h1>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-lg">
                    <p><strong>Evaluator:</strong> {evaluation.evaluator.fullName}</p>
                    <p><strong>Evaluated:</strong> {evaluation.evaluated.fullName}</p>
                </div>
                <p className="mt-4"><strong>Status:</strong> 
                    <span className={`font-semibold 
                        ${evaluation.status === 'COMPLETED' ? 'text-green-500' : ''}
                        ${evaluation.status === 'IN_PROGRESS' ? 'text-blue-500' : ''}
                        ${evaluation.status === 'NOT_STARTED' ? 'text-yellow-500' : ''}
                    `}>
                        {evaluation.status.replace('_', ' ')}
                    </span>
                </p>
            </div>

            <div>
                <h2 className="text-xl font-semibold mb-4">Answers</h2>
                <div className="space-y-4">
                    {evaluation.answers.map((answer) => (
                        <div key={answer.answerId} className="p-4 border rounded bg-white shadow-sm">
                            <label className="block text-gray-700 font-semibold">{answer.questionText}</label>
                            <div className="mt-3">
                                <QuestionRenderer
                                    question={answer}
                                    value={answer.currentValue}
                                    disabled={true}
                                />
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default AdminEvaluationDetailsPage;
