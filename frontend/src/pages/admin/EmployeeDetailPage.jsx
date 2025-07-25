import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import UserService from '../../services/userService';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Button } from '../../components/ui/button';
import { toast } from 'react-toastify';

const EmployeeDetailPage = () => {
    const { userId } = useParams();
    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);
    const [assignments, setAssignments] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        if (selectedPeriod && userId) {
            setLoading(true);
            UserService.getUserAssignments(selectedPeriod.id, userId)
                .then(response => {
                    setAssignments(response.data);
                })
                .catch(error => {
                    toast.error('Çalışan verileri getirilirken bir hata oluştu.');
                    console.error('Error fetching employee assignments:', error);
                })
                .finally(() => {
                    setLoading(false);
                });
        }
    }, [selectedPeriod, userId]);

    if (loading) {
        return <div className="p-8">Yükleniyor...</div>;
    }

    if (!selectedPeriod) {
        return <div className="p-8 text-center">Lütfen bir değerlendirme dönemi seçin.</div>;
    }

    if (!assignments) {
        return <div className="p-8">Çalışan verileri bulunamadı.</div>;
    }

    const { user, evaluationsMade, evaluationsReceived } = assignments;

    const AssignmentList = ({ title, data, isEvaluationsMade }) => (
        <Card>
            <CardHeader>
                <CardTitle>{title}</CardTitle>
            </CardHeader>
            <CardContent>
                {data.length > 0 ? (
                    <ul className="space-y-4">
                        {data.map(assignment => (
                            <li key={assignment.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-md">
                                <div>
                                    <p className="font-semibold">{`${assignment.evaluatorUser.firstName} ${assignment.evaluatorUser.lastName}`}</p>
                                    <p className="text-sm text-gray-500">{assignment.evaluator.name}</p>
                                </div>
                                <Button 
                                    variant="outline" 
                                    size="sm" 
                                    onClick={() => navigate(`/dashboard/assignments/${assignment.id}/answers`, {
                                        state: {
                                            user: isEvaluationsMade ? assignment.evaluatorUser : user,
                                            evaluatorUser: isEvaluationsMade ? user : assignment.evaluatorUser,
                                            evaluator: assignment.evaluator
                                        }
                                    })}
                                >
                                    Yanıtları Gör
                                </Button>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-500">Bu kategori için değerlendirme bulunmamaktadır.</p>
                )}
            </CardContent>
        </Card>
    );

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <div className="mb-8">
                <h1 className="text-3xl font-bold">{`${user.firstName} ${user.lastName}`}</h1>
                <div className="flex items-center gap-4 mt-2">
                    <p className="text-gray-600">{user.email}</p>
                    <Badge variant={user.isActive ? "default" : "destructive"}>
                        {user.isActive ? 'Aktif' : 'Pasif'}
                    </Badge>
                </div>
            </div>

            <div className="grid md:grid-cols-2 gap-8">
                <AssignmentList title="Yaptığı Değerlendirmeler" data={evaluationsMade} isEvaluationsMade={true} />
                <AssignmentList title="Hakkındaki Değerlendirmeler" data={evaluationsReceived} isEvaluationsMade={false} />
            </div>
        </div>
    );
};

export default EmployeeDetailPage;
