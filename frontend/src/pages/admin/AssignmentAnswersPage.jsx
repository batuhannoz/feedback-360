import React, { useEffect, useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import UserService from '../../services/userService';
import { toast } from 'react-toastify';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { ArrowLeft } from 'lucide-react';

const AssignmentAnswersPage = () => {
    const { assignmentId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();
    const { user, evaluatorUser, evaluator } = location.state || {};

    const [answers, setAnswers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (assignmentId) {
            UserService.getAnswersForAssignment(assignmentId)
                .then(response => {
                    setAnswers(response.data || []);
                })
                .catch(error => {
                    toast.error('Yanıtlar getirilirken bir hata oluştu.');
                    console.error('Error fetching answers:', error);
                })
                .finally(() => {
                    setLoading(false);
                });
        }
    }, [assignmentId]);

    if (loading) {
        return <div className="p-8">Yanıtlar Yükleniyor...</div>;
    }

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <div className="mb-8">
                <Button variant="outline" size="sm" onClick={() => navigate(-1)} className="mb-4">
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    Geri Dön
                </Button>
                <h1 className="text-3xl font-bold">Değerlendirme Yanıtları</h1>
                {user && evaluatorUser && (
                    <p className="text-lg text-gray-600 mt-2">
                        <span className="font-semibold">{`${evaluatorUser.firstName} ${evaluatorUser.lastName}`}</span> tarafından
                        <span className="font-semibold"> {`${user.firstName} ${user.lastName}`} </span>
                        için yapılan değerlendirme.
                        (<span className="italic">{evaluator.name}</span>)
                    </p>
                )}
            </div>

            <div className="space-y-6">
                {answers.length > 0 ? (
                    answers.map(answer => (
                        <Card key={answer.id}>
                            <CardHeader>
                                <CardTitle className="text-lg">{answer.question.questionText}</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="flex items-center gap-4">
                                    <p className="font-semibold">Puan:</p>
                                    <div className="flex items-center justify-center h-10 w-10 rounded-full bg-primary text-primary-foreground">
                                        {answer.score !== 0 ? answer.score : '-'}
                                    </div>
                                </div>
                                {answer.comment && (
                                    <div className="mt-4">
                                        <p className="font-semibold">Yorum:</p>
                                        <p className="text-gray-700 p-3 bg-gray-100 rounded-md mt-1">{answer.comment}</p>
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    ))
                ) : (
                    <p>Bu değerlendirme için yanıt bulunmamaktadır.</p>
                )}
            </div>
        </div>
    );
};

export default AssignmentAnswersPage;
