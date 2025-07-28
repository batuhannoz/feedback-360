import React, { useEffect, useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import UserService from '../../services/userService';
import { toast } from 'react-toastify';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { ArrowLeft } from 'lucide-react';
import { Badge } from '../../components/ui/badge';

const AssignmentAnswersPage = () => {
    const { assignmentId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();
    const { user, evaluatorUser } = location.state || {};

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
                    <p className="text-gray-600">
                        <span className="font-semibold">{`${evaluatorUser.firstName} ${evaluatorUser.lastName}`}</span> tarafından
                        <span className="font-semibold"> {`${user.firstName} ${user.lastName}`} </span>
                        için yapılan değerlendirme.
                    </p>
                )}
            </div>

            <div className="space-y-6">
                {answers.length > 0 ? (
                    answers.map(answer => (
                        <Card key={answer.id}>
                            <CardHeader>
                                <CardTitle className="text-lg">{answer.questionText}</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="flex items-center gap-4">
                                    <Badge variant={answer.score === 0 ? 'secondary' : answer.score > 3 ? 'success' : answer.score < 3 ? 'destructive' : 'warning'}>
                                        Puan: {answer.score === 0 ? 'Fikrim Yok' : answer.score}
                                    </Badge>
                                    {answer.answerText && (
                                        <p className="text-gray-700 italic">{`"${answer.answerText}"`}</p>
                                    )}
                                </div>
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
