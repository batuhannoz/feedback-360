import React, { useEffect, useState } from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import evaluationPeriodService from '../../services/evaluationPeriodService';
import { toast } from 'react-toastify';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "../../components/ui/table.jsx";
import { Badge } from "../../components/ui/badge.jsx";
import { Progress } from "../../components/ui/progress.jsx";

const EvaluationPeriodDetailPage = () => {
    const { periodId } = useParams();
    const [periodDetails, setPeriodDetails] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();


    useEffect(() => {
        const fetchPeriodDetails = async () => {
            try {
                const response = await evaluationPeriodService.getPeriodDetails(periodId);
                setPeriodDetails(response.data);
            } catch (error) {
                toast.error('Dönem detayları getirilemedi.');
            } finally {
                setLoading(false);
            }
        };

        fetchPeriodDetails();
    }, [periodId]);

    if (loading) {
        return <div>Yükleniyor...</div>;
    }

    if (!periodDetails) {
        return <div>Dönem bulunamadı.</div>;
    }

    const getStatusVariant = (status) => {
        switch (status) {
            case 'IN_PROGRESS':
                return 'default';
            case 'COMPLETED':
                return 'success';
            case 'NOT_STARTED':
                return 'secondary';
            default:
                return 'destructive';
        }
    };

    return (
        <div className="container mx-auto p-6">
            <Card className="mb-6">
                <CardHeader>
                    <CardTitle className="text-2xl">{periodDetails.periodName}</CardTitle>
                    <div className="flex items-center justify-between text-sm text-muted-foreground">
                        <span>{new Date(periodDetails.startDate).toLocaleDateString()} - {new Date(periodDetails.endDate).toLocaleDateString()}</span>
                        <Badge variant={getStatusVariant(periodDetails.status)}>{periodDetails.status}</Badge>
                    </div>
                </CardHeader>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Katılımcılar</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Ad Soyad</TableHead>
                                <TableHead>Email</TableHead>
                                <TableHead className="text-center">İlerleme</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {periodDetails.participants.map(participant => {
                                const completionRate = participant.totalAssignments > 0
                                    ? (participant.completedAssignments / participant.totalAssignments) * 100
                                    : 0;
                                return (
                                    <TableRow key={participant.userId} onClick={() => navigate(`/dashboard/employees/${participant.userId}`)}>
                                        <TableCell>{participant.firstName} {participant.lastName}</TableCell>
                                        <TableCell>{participant.email}</TableCell>
                                        <TableCell>
                                            <div className="flex items-center justify-center gap-2">
                                                <Progress value={completionRate} className="w-3/4" />
                                                <span className="text-sm font-medium w-1/4 text-right">{completionRate.toFixed(0)}%</span>
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    );
};

export default EvaluationPeriodDetailPage;

