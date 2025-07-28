import React, { useEffect, useState } from 'react';
import DashboardService from '../../services/dashboardService';
import { toast } from 'react-toastify';
import { Link } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Progress } from '../../components/ui/progress';
import { Users, Calendar, CheckCircle, Activity } from 'lucide-react';

const DashboardPage = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        DashboardService.getDashboardStats()
            .then(response => {
                setStats(response.data);
            })
            .catch(error => {
                toast.error('Dashboard verileri getirilirken bir hata oluştu.');
                console.error('Error fetching dashboard stats:', error);
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    if (loading) {
        return <div className="flex items-center justify-center h-full">Yükleniyor...</div>;
    }

    const completionRate = stats && stats.totalAssignments > 0
        ? (stats.completedAssignments / stats.totalAssignments) * 100
        : 0;

    return (
        <div className="p-4 md:p-8">
            <h1 className="text-3xl font-bold mb-6">Yönetim Paneli</h1>
            {stats ? (
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                    <Link to="/dashboard/employees">
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Toplam Kullanıcı</CardTitle>
                                <Users className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold">{stats.totalUsers}</div>
                            </CardContent>
                        </Card>
                    </Link>
                    <Link to="/dashboard/evaluations">
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Aktif Dönemler</CardTitle>
                                <Calendar className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold">{stats.activePeriods}</div>
                            </CardContent>
                        </Card>
                    </Link>
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">Tamamlanan Değerlendirmeler</CardTitle>
                            <CheckCircle className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">{stats.completedAssignments} / {stats.totalAssignments}</div>
                        </CardContent>
                    </Card>
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">Tamamlanma Oranı</CardTitle>
                            <Activity className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold mb-2">{completionRate.toFixed(1)}%</div>
                            <Progress value={completionRate} />
                        </CardContent>
                    </Card>
                </div>
            ) : (
                <p>Dashboard verileri yüklenemedi.</p>
            )}

            {stats && stats.activePeriodStats && stats.activePeriodStats.length > 0 && (
                <div className="mt-8">
                    <h2 className="text-2xl font-bold mb-4">Aktif Dönemler</h2>
                    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                        {stats.activePeriodStats.map(period => {
                            const completionRate = period.totalAssignments > 0
                                ? (period.completedAssignments / period.totalAssignments) * 100
                                : 0;
                            return (
                                <Link to={`/dashboard/evaluations/${period.id}`} key={period.id}>
                                    <Card>
                                    <CardHeader>
                                        <CardTitle>{period.name}</CardTitle>
                                        <p className="text-sm text-muted-foreground">
                                            {new Date(period.startDate).toLocaleDateString()} - {new Date(period.endDate).toLocaleDateString()}
                                        </p>
                                    </CardHeader>
                                    <CardContent>
                                        <div className="flex justify-between items-center mb-2">
                                            <span className="text-sm font-medium">Tamamlanma Oranı</span>
                                            <span className="text-sm font-bold">{completionRate.toFixed(1)}%</span>
                                        </div>
                                        <Progress value={completionRate} />
                                        <p className="text-right text-sm text-muted-foreground mt-2">
                                            {period.completedAssignments} / {period.totalAssignments} tamamlandı
                                        </p>
                                    </CardContent>
                                    </Card>
                                </Link>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
};

export default DashboardPage;
