import React, {useEffect, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import UserEvaluationService from '../../services/userEvaluationService.js';
import {toast} from 'react-toastify';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '../../components/ui/card.jsx';
import {Button} from '../../components/ui/button.jsx';
import {Textarea} from '../../components/ui/textarea.jsx';
import {Label} from '../../components/ui/label.jsx';
import {ArrowLeft} from 'lucide-react';

const EvaluationPage = () => {
    const { periodId, evaluatedUserId } = useParams();
    const [questions, setQuestions] = useState([]);
    const [answers, setAnswers] = useState({});
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        if (periodId && evaluatedUserId) {
            UserEvaluationService.getQuestionsForEvaluation(periodId, evaluatedUserId)
                .then(response => {
                    setQuestions(response.data || []);
                })
                .catch(error => {
                    toast.error('Sorular getirilirken bir hata oluştu.');
                    console.error('Error fetching questions:', error);
                })
                .finally(() => {
                    setLoading(false);
                });
        }
    }, [periodId, evaluatedUserId]);

    const handleAnswerChange = (questionId, field, value) => {
        setAnswers(prev => ({
            ...prev,
            [questionId]: {
                ...prev[questionId],
                [field]: value
            }
        }));
    };

    const handleSubmit = () => {
        if (Object.keys(answers).length !== questions.length || Object.values(answers).some(a => typeof a.score === 'undefined')) {
            toast.warn('Lütfen tüm sorular için bir puan seçin.');
            return;
        }

        for (const question of questions) {
            const answer = answers[question.id];
            const isCommentRequired = question.scoresRequiringComment.includes(answer.score);
            const hasComment = answer.comment && answer.comment.trim() !== '';

            if (isCommentRequired && !hasComment) {
                toast.warn(`"${question.questionText}" sorusu için seçtiğiniz puana göre yorum yapmanız zorunludur.`);
                return;
            }
        }

        const submissionData = Object.entries(answers).map(([questionId, answer]) => ({
            questionId: parseInt(questionId),
            score: answer.score,
            answerText: answer.comment || ''
        }));

        UserEvaluationService.submitAnswers(periodId, evaluatedUserId, submissionData)
            .then(() => {
                toast.success('Değerlendirmeniz başarıyla gönderildi.');
                navigate(`/my-evaluations/${periodId}/assignments`);
            })
            .catch(error => {
                toast.error('Yanıtlar gönderilirken bir hata oluştu.');
                console.error('Error submitting answers:', error);
            });
    };

    if (loading) {
        return <div className="flex items-center justify-center h-screen">Sorular Yükleniyor...</div>;
    }

    return (
        <div className="p-4 sm:p-8 bg-gray-50 min-h-screen">
            <div className="max-w-4xl mx-auto">
                <Button variant="outline" size="sm" onClick={() => navigate(`/my-evaluations/${periodId}/assignments`)} className="mb-4">
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    Geri Dön
                </Button>
                <Card>
                    <CardHeader>
                        <CardTitle className="text-2xl">Değerlendirme Formu</CardTitle>
                        <CardDescription>Lütfen aşağıdaki soruları dikkatlice yanıtlayın.</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-8">
                            {questions.map(question => {
                                const currentAnswer = answers[question.id];
                                const isCommentRequired = currentAnswer && question.scoresRequiringComment.includes(currentAnswer.score);

                                return (
                                    <div key={question.id} className="p-6 border rounded-lg space-y-6 bg-white shadow-sm">
                                        {/* Question Text */}
                                        <Label className="text-lg font-semibold text-gray-800">{question.questionText}</Label>

                                        {/* Score Selection */}
                                        <div className="space-y-3">
                                            <Label className="text-base font-medium">Puan</Label>
                                            <div className="flex flex-wrap gap-2">
                                                {[1, 2, 3, 4, 5, 0].map(score => {
                                                    if (question.hiddenScores.includes(score)) return null;
                                                    const isSelected = currentAnswer?.score === score;
                                                    return (
                                                        <Button
                                                            key={score}
                                                            variant={isSelected ? 'default' : 'outline'}
                                                            onClick={() => handleAnswerChange(question.id, 'score', score)}
                                                            className="w-28"
                                                        >
                                                            {score === 0 ? 'Fikrim Yok' : score}
                                                        </Button>
                                                    );
                                                })}
                                            </div>
                                        </div>

                                        {/* Comment Section */}
                                        <div className="space-y-3">
                                            <Label htmlFor={`comment-${question.id}`} className="text-base font-medium">
                                                Yorum {isCommentRequired && <span className="text-red-500">*</span>}
                                            </Label>
                                            <Textarea
                                                id={`comment-${question.id}`}
                                                value={currentAnswer?.comment || ''}
                                                onChange={(e) => handleAnswerChange(question.id, 'comment', e.target.value)}
                                                placeholder="Eklemek istediğiniz bir yorum varsa buraya yazın..."
                                                required={isCommentRequired}
                                                className={isCommentRequired ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}
                                            />
                                            {isCommentRequired && (
                                                <p className="text-sm text-red-600 mt-1">Bu puan için yorum yapmanız zorunludur.</p>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                        <div className="flex justify-end mt-8">
                            <Button onClick={handleSubmit} size="lg">Değerlendirmeyi Gönder</Button>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default EvaluationPage;
