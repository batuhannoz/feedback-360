import React, {useState, useEffect, useCallback} from 'react';
import {useParams} from 'react-router-dom';
import {useSelector} from 'react-redux';
import ReportService from '../../services/reportService';

import ReportHeader from '../../components/admin/reports/ReportHeader';
import OverallScores from '../../components/admin/reports/OverallScores';
import ScoresByEvaluator from '../../components/admin/reports/ScoresByEvaluator';
import CompetencyReport from '../../components/admin/reports/CompetencyReport';
import ReportSettingsPanel from '../../components/admin/reports/ReportSettingsPanel';
import CommentsSection from '../../components/admin/reports/CommentsSection';

const defaultReportSettings = {
    showOverallResults: true,
    showSourceBasedCompetencyScores: true,
    showDetailedCompetencyScores: true,
    showCommentsSection: true,
    showDetailedGraph: true,
    showDetailedQuestions: true,
    showDetailedCompetencyRawScore: true,
    showDetailedCompetencyWeightedScore: true,
    showSelfColumn: true,
    showPeerColumn: true,
    showManagerColumn: true,
    showSubordinateColumn: true,
    showOtherColumn: true,
    showAverageColumn: true,
};

const EmployeeReportPage = () => {
    const {userId} = useParams();
    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);
    const [reportData, setReportData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [reportSettings, setReportSettings] = useState(defaultReportSettings);

    const fetchReport = useCallback(() => {
        if (selectedPeriod && userId) {
            setLoading(true);
            console.log(reportSettings)
            ReportService.getUserPeriodReport(selectedPeriod.id, userId, reportSettings)
                .then(response => {
                    setReportData(response.data);
                })
                .catch(error => {
                    setReportData(null);
                    console.error('Error fetching employee report:', error);
                })
                .finally(() => {
                    setLoading(false);
                });
        }
    }, [selectedPeriod, userId, reportSettings]);

    useEffect(() => {
        fetchReport();
    }, [userId, selectedPeriod, fetchReport]);

    if (!selectedPeriod) {
        return <div className="p-8 text-center">Lütfen bir değerlendirme dönemi seçin.</div>;
    }

    if (loading) {
        return <div className="p-8">Yükleniyor...</div>;
    }

    return (
        <div className="p-8 bg-gray-50 min-h-screen space-y-6">
            <ReportSettingsPanel
                settings={reportSettings}
                onSettingsChange={setReportSettings}
                onGenerateReport={fetchReport}
            />

            {reportData ? (
                <>
                    <ReportHeader user={reportData.user} period={reportData.period}/>

                    {reportData.overallScores && <OverallScores scores={reportData.overallScores}/>}

                    {reportData.scoresByEvaluatorType && (
                        <ScoresByEvaluator scores={reportData.scoresByEvaluatorType}/>
                    )}

                    {reportData.competencyScores && <CompetencyReport competencies={reportData.competencyScores}/>}

                    {reportData.comments && <CommentsSection comments={reportData.comments}/>}
                </>
            ) : (
                <div className="p-8 text-center bg-white rounded-md shadow">Bu kullanıcı veya dönem için gösterilecek
                    rapor verisi bulunamadı.</div>
            )}
        </div>
    );
};

export default EmployeeReportPage;