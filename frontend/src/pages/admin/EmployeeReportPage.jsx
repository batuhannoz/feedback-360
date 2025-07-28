import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useSelector } from 'react-redux';
import ReportService from '../../services/reportService';
import { toast } from 'react-toastify';
import ReportHeader from '../../components/admin/reports/ReportHeader';
import OverallScores from '../../components/admin/reports/OverallScores';
import ScoresByEvaluator from '../../components/admin/reports/ScoresByEvaluator';
import CompetencyReport from '../../components/admin/reports/CompetencyReport';
import {Card, CardContent, CardHeader, CardTitle} from "../../components/ui/card.jsx";

const EmployeeReportPage = () => {
    const { userId } = useParams();
    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);
    const [reportData, setReportData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (selectedPeriod && userId) {
            setLoading(true);
            ReportService.getUserPeriodReport(selectedPeriod.id, userId)
                .then(response => {
                    setReportData(response.data);
                })
                .catch(error => {
                    toast.error('Rapor verileri getirilirken bir hata oluştu.');
                    console.error('Error fetching employee report:', error);
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

    if (!reportData) {
        return <div className="p-8">Rapor verileri bulunamadı.</div>;
    }

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <ReportHeader user={reportData.user} period={reportData.period} />
            <OverallScores reportData={reportData} />
            <div className="grid md:grid-cols-2 gap-6 mt-6">
                <ScoresByEvaluator scores={reportData.scoresByEvaluator} />
                {/* Placeholder for a chart */}
                <Card>
                    <CardHeader><CardTitle>Puan Dağılımı</CardTitle></CardHeader>
                    <CardContent>
                        <p className="text-muted-foreground">Chart will be implemented here.</p>
                    </CardContent>
                </Card>
            </div>
            <div className="mt-6">
                <CompetencyReport competencies={reportData.competencyScores} />
            </div>
        </div>
    );
};

export default EmployeeReportPage;
