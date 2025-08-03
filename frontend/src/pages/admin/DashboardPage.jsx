import React, {useEffect, useState} from 'react';
import DashboardService from '../../services/dashboardService';
import {toast} from 'react-toastify';
import {Link} from "react-router-dom";
import {Card, CardContent, CardHeader, CardTitle} from '../../components/ui/card';
import {Button} from '../../components/ui/button';
import {Progress} from '../../components/ui/progress';
import {Users, Calendar, CheckCircle, Activity, PlusCircle} from 'lucide-react';

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
        return <div className="flex items-center justify-center h-screen">Yükleniyor...</div>;
    }

    const completionRate = stats && stats.totalAssignments > 0
        ? (stats.completedAssignments / stats.totalAssignments) * 100
        : 0;

    return (
        <div className="p-4 md:p-8">
            <div className="flex flex-wrap justify-between items-center gap-4 mb-6">
                <h1 className="text-3xl font-bold">Yönetim Paneli</h1>
                <Link to="/dashboard/evaluations">
                    <Button>
                        <PlusCircle className="mr-2 h-4 w-4"/>
                        Yeni Dönem Oluştur
                    </Button>
                </Link>
            </div>

            {stats ? (
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                    <Link to="/dashboard/employees" className="h-full">
                        <Card className="h-full flex flex-col">
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Toplam Kullanıcı</CardTitle>
                                <Users className="h-4 w-4 text-muted-foreground"/>
                            </CardHeader>
                            <CardContent className="flex-grow flex items-end">
                                <div className="text-2xl font-bold">{stats.totalUsers}</div>
                            </CardContent>
                        </Card>
                    </Link>

                    <Link to="/dashboard/evaluations" className="h-full">
                        <Card className="h-full flex flex-col">
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Aktif Dönemler</CardTitle>
                                <Calendar className="h-4 w-4 text-muted-foreground"/>
                            </CardHeader>
                            <CardContent className="flex-grow flex items-end">
                                <div className="text-2xl font-bold">{stats.activePeriods}</div>
                            </CardContent>
                        </Card>
                    </Link>

                    <Card className="h-full flex flex-col">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">Tamamlanan Değerlendirmeler</CardTitle>
                            <CheckCircle className="h-4 w-4 text-muted-foreground"/>
                        </CardHeader>
                        <CardContent className="flex-grow flex items-end">
                            <div
                                className="text-2xl font-bold">{stats.completedAssignments} / {stats.totalAssignments}</div>
                        </CardContent>
                    </Card>

                    <Card className="h-full flex flex-col">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">Genel Tamamlanma Oranı</CardTitle>
                            <Activity className="h-4 w-4 text-muted-foreground"/>
                        </CardHeader>
                        <CardContent className="flex-grow flex flex-col justify-end">
                            <div className="text-2xl font-bold mb-2">{completionRate.toFixed(1)}%</div>
                            <Progress value={completionRate}/>
                        </CardContent>
                    </Card>
                </div>
            ) : (
                <p>Dashboard verileri yüklenemedi.</p>
            )}

            {stats && stats.activePeriodStats && stats.activePeriodStats.length > 0 && (
                <div className="mt-8">
                    <h2 className="text-2xl font-bold mb-4">Aktif Dönemler Detayları</h2>
                    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                        {stats.activePeriodStats.map(period => {
                            const periodCompletionRate = period.totalAssignments > 0
                                ? (period.completedAssignments / period.totalAssignments) * 100
                                : 0;
                            return (
                                <Link to={`/dashboard/evaluations/${period.id}`} key={period.id} className="h-full">
                                    <Card className="h-full flex flex-col hover:border-primary transition-colors">
                                        <CardHeader>
                                            <CardTitle>{period.name}</CardTitle>
                                            <p className="text-sm text-muted-foreground pt-1">
                                                {new Date(period.startDate).toLocaleDateString()} - {new Date(period.endDate).toLocaleDateString()}
                                            </p>
                                        </CardHeader>
                                        <CardContent className="flex-grow flex flex-col justify-end">
                                            <div className="flex justify-between items-center mb-2">
                                                <span className="text-sm font-medium">Tamamlanma Oranı</span>
                                                <span
                                                    className="text-sm font-bold">{periodCompletionRate.toFixed(1)}%</span>
                                            </div>
                                            <Progress value={periodCompletionRate}/>
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