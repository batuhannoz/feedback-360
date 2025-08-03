import React, {useEffect, useState} from 'react';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import {useNavigate, useParams} from 'react-router-dom';
import UserEvaluationService from '../../services/userEvaluationService.js';
import {toast} from 'react-toastify';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '../../components/ui/card.jsx';
import {Button} from '../../components/ui/button.jsx';
import {ArrowLeft, ChevronRight} from 'lucide-react';

const MyAssignmentsPage = () => {
    const { periodId } = useParams();
    const [assignments, setAssignments] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        if (periodId) {
            UserEvaluationService.getMyEvaluationsForPeriod(periodId)
                .then(response => {
                    setAssignments(response.data || []);
                })
                .catch(error => {
                    toast.error('Değerlendirilecek kişiler getirilirken bir hata oluştu.');
                    console.error('Error fetching assignments:', error);
                })
                .finally(() => {
                    setLoading(false);
                });
        }
    }, [periodId]);

    if (loading) {
        return <div className="flex justify-center items-center h-screen"><LoadingSpinner /></div>;
    }

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <div className="max-w-4xl mx-auto">
                <Button variant="outline" size="sm" onClick={() => navigate('/my-evaluations')} className="mb-4">
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    Dönemlere Geri Dön
                </Button>
                <Card>
                    <CardHeader>
                        <CardTitle className="text-2xl">Değerlendirilecek Kişiler</CardTitle>
                        <CardDescription>Bu dönemde değerlendirmeniz gereken kişiler aşağıda listelenmiştir.</CardDescription>
                    </CardHeader>
                    <CardContent>
                        {assignments.length > 0 ? (
                            <ul className="space-y-4">
                                {assignments.map(user => (
                                    <li key={user.id}>
                                        <button 
                                            onClick={() => navigate(`/my-evaluations/${periodId}/assignments/${user.id}`)}
                                            className="w-full text-left p-4 border rounded-lg hover:bg-gray-100 transition-colors flex justify-between items-center"
                                        >
                                            <div>
                                                <p className="font-semibold text-lg">{`${user.firstName} ${user.lastName}`}</p>
                                                <p className="text-sm text-gray-500">{user.email}</p>
                                            </div>
                                            <ChevronRight className="h-5 w-5 text-gray-400" />
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p>Bu dönem için değerlendirilecek kimse bulunmamaktadır.</p>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default MyAssignmentsPage;
