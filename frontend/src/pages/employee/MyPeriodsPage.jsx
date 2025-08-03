import React, {useEffect, useState} from 'react';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import {useNavigate} from 'react-router-dom';
import UserEvaluationService from '../../services/userEvaluationService.js';
import {toast} from 'react-toastify';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '../../components/ui/card.jsx';
import {ChevronRight} from 'lucide-react';

const MyPeriodsPage = () => {
    const [periods, setPeriods] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        UserEvaluationService.getMyEvaluationPeriods()
            .then(response => {
                setPeriods(response.data || []);
            })
            .catch(error => {
                toast.error('Değerlendirme dönemleri getirilirken bir hata oluştu.');
                console.error('Error fetching my periods:', error);
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    if (loading) {
        return <div className="flex justify-center items-center h-screen"><LoadingSpinner /></div>;
    }

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <Card className="max-w-4xl mx-auto">
                <CardHeader>
                    <CardTitle className="text-2xl">Değerlendirme Dönemlerim</CardTitle>
                    <CardDescription>Katılmanız gereken aktif değerlendirme dönemleri aşağıda listelenmiştir.</CardDescription>
                </CardHeader>
                <CardContent>
                    {periods.length > 0 ? (
                        <ul className="space-y-4">
                            {periods.map(period => (
                                <li key={period.id}>
                                    <button 
                                        onClick={() => navigate(`/my-evaluations/${period.id}/assignments`)}
                                        className="w-full text-left p-4 border rounded-lg hover:bg-gray-100 transition-colors flex justify-between items-center"
                                    >
                                        <div>
                                            <p className="font-semibold text-lg">{period.periodName}</p>
                                            <p className="text-sm text-gray-500">Durum: {period.status}</p>
                                        </div>
                                        <ChevronRight className="h-5 w-5 text-gray-400" />
                                    </button>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p>Henüz atanmış bir değerlendirme döneminiz bulunmamaktadır.</p>
                    )}
                </CardContent>
            </Card>
        </div>
    );
};

export default MyPeriodsPage;
