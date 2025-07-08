import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import EmployeeService from '../../services/employeeService';
import { SubmitAnswersRequest } from '../../models/request/SubmitAnswersRequest';
import QuestionRenderer from '../../components/QuestionRenderer';

const EvaluationPage = () => {
    const { evaluationId } = useParams();
    const navigate = useNavigate();
    const [evaluation, setEvaluation] = useState(null);
    const [answers, setAnswers] = useState({});
    const [isSubmittable, setIsSubmittable] = useState(false);

    useEffect(() => {
        const fetchEvaluationDetails = async () => {
            try {
                const response = await EmployeeService.getEvaluationDetails(evaluationId);
                const evalData = response.data;
                setEvaluation(evalData);
                setIsSubmittable(evalData.status !== 'COMPLETED');

                const initialAnswers = {};
                evalData.answers.forEach(answer => {
                    initialAnswers[answer.answerId] = answer.currentValue || '';
                });
                setAnswers(initialAnswers);
            } catch (error) {
                console.error('Error fetching evaluation details:', error);
            }
        };

        fetchEvaluationDetails();
    }, [evaluationId]);

    const handleAnswerChange = (answerId, value) => {
        setAnswers(prev => ({ ...prev, [answerId]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!isSubmittable) return;

        try {
            const answerPayload = Object.entries(answers).map(([answerId, value]) => ({ 
                answerId: parseInt(answerId),
                value 
            }));
            const request = new SubmitAnswersRequest(answerPayload);
            await EmployeeService.submitAnswers(evaluationId, request);
            navigate('/employee/dashboard');
        } catch (error) {
            console.error('Error submitting answers:', error);
        }
    };

    if (!evaluation) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Evaluation for: {evaluation.evaluated.fullName}</h1>
            <p className="mb-4">Status: 
                <span className={`font-semibold 
                    ${evaluation.status === 'COMPLETED' ? 'text-green-500' : ''}
                    ${evaluation.status === 'IN_PROGRESS' ? 'text-blue-500' : ''}
                    ${evaluation.status === 'NOT_STARTED' ? 'text-yellow-500' : ''}
                `}>
                    {evaluation.status.replace('_', ' ')}
                </span>
            </p>
            <form onSubmit={handleSubmit}>
                {evaluation.answers.map((answer) => (
                    <div key={answer.answerId} className="mb-4 p-4 border rounded bg-white shadow-sm">
                        <label className="block text-gray-700 font-semibold">{answer.questionText}</label>
                        <div className="mt-2">
                            <QuestionRenderer
                                question={answer}
                                value={answers[answer.answerId]}
                                onChange={(value) => handleAnswerChange(answer.answerId, value)}
                                disabled={!isSubmittable}
                            />
                        </div>
                    </div>
                ))}
                {isSubmittable && (
                    <div className="flex justify-end mt-6">
                        <button type="submit" className="px-6 py-2 text-white bg-blue-600 rounded hover:bg-blue-700 disabled:bg-gray-400">
                            Submit Answers
                        </button>
                    </div>
                )}
            </form>
        </div>
    );
};

export default EvaluationPage;
