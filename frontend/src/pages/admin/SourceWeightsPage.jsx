import React, { useState, useEffect, useCallback } from 'react';
import { useSelector } from 'react-redux';
import PeriodCompetencyService from '../../services/periodCompetencyService';
import { getEvaluatorsByPeriodId } from '../../services/periodEvaluatorService';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { toast } from 'react-toastify';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Terminal } from 'lucide-react';

const SourceWeightsPage = () => {
    const [competencies, setCompetencies] = useState([]);
    const [evaluators, setEvaluators] = useState([]);
    const [weights, setWeights] = useState({});
    const [loading, setLoading] = useState(false);
    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);

    const fetchData = useCallback(async () => {
        if (!selectedPeriod?.id) return;
        setLoading(true);
        try {
            const [competenciesRes, evaluatorsRes] = await Promise.all([
                PeriodCompetencyService.getCompetenciesByPeriod(selectedPeriod.id),
                getEvaluatorsByPeriodId(selectedPeriod.id)
            ]);

            const activeEvaluators = evaluatorsRes.data;
            setEvaluators(activeEvaluators);

            const competenciesData = competenciesRes.data;
            const weightsData = {};

            await Promise.all(competenciesData.map(async (comp) => {
                console.log(comp)
                const permissionsRes = await PeriodCompetencyService.getCompetencyEvaluatorPermissions(selectedPeriod.id, comp.id);
                weightsData[comp.id] = {};
                permissionsRes.data.forEach(perm => {
                    weightsData[comp.id][perm.evaluatorId] = perm.weight;
                });
            }));

            setCompetencies(competenciesData);
            setWeights(weightsData);
        } catch (error) {
            toast.error('Veriler getirilirken bir hata oluştu.');
        }
        setLoading(false);
    }, [selectedPeriod?.id]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handleWeightChange = (competencyId, evaluatorId, value) => {
        const newWeight = parseInt(value, 10);
        if (isNaN(newWeight) && value !== '') return;

        setWeights(prev => ({
            ...prev,
            [competencyId]: {
                ...prev[competencyId],
                [evaluatorId]: isNaN(newWeight) ? 0 : newWeight
            }
        }));
    };

    const handleSave = async () => {
        for (const compId in weights) {
            const total = Object.values(weights[compId]).reduce((sum, w) => sum + w, 0);
            if (total !== 100) {
                const competency = competencies.find(c => c.id === parseInt(compId));
                toast.error(`${competency?.title || 'Bir yetkinlik'} için ağırlıklar toplamı 100 olmalıdır.`);
                return;
            }
        }

        setLoading(true);
        try {
            const savePromises = Object.keys(weights).map(competencyId => {
                const weightsForCompetency = Object.keys(weights[competencyId]).map(evaluatorId => ({
                    evaluatorId: parseInt(evaluatorId),
                    weight: weights[competencyId][evaluatorId]
                }));

                const requestBody = { weights: weightsForCompetency };

                return PeriodCompetencyService.setCompetencyEvaluatorWeights(
                    selectedPeriod.id,
                    parseInt(competencyId),
                    requestBody
                );
            });

            await Promise.all(savePromises);

            toast.success('Tüm ağırlıklar başarıyla güncellendi.');
        } catch (error) {
            console.error("Ağırlık güncelleme hatası:", error); // Geliştirme için hatayı konsola yazdır
            toast.error('Ağırlıkları güncellerken bir hata oluştu.');
        } finally {
            setLoading(false);
        }
    };

    const getRowTotal = (competencyId) => {
        return Object.values(weights[competencyId] || {}).reduce((sum, w) => sum + (w || 0), 0);
    };

    return (
        <div className="p-8">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Kaynak Ağırlıkları</h1>
                <div className="flex gap-2">
                    <Button variant="outline" onClick={fetchData}>İptal</Button>
                    <Button onClick={handleSave} disabled={loading}>{loading ? 'Kaydediliyor...' : 'Kaydet'}</Button>
                </div>
            </div>

            <Alert className="mb-6">
                <Terminal className="h-4 w-4" />
                <AlertTitle>Kaynak Ağırlıklarını Belirleyin</AlertTitle>
                <AlertDescription>
                    Aşağıdaki tabloda, her bir yetkinlik için farklı değerlendirici kaynaklarının (Yönetici, Akran, vb.) vereceği cevapların ağırlıklarını belirleyebilirsiniz. Her satırın (yetkinlik) toplamı %100 olmalıdır.
                </AlertDescription>
            </Alert>

            <div className="bg-white shadow-md rounded-lg overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Yetkinlik</th>
                        {evaluators.map(e => <th key={e.id} className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">{e.name} (%)</th>)}
                        <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Toplam</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {competencies.map(comp => {
                        const total = getRowTotal(comp.id);
                        return (
                            <tr key={comp.id}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{comp.title}</td>
                                {evaluators.map(e => {
                                    // Check if the evaluator has permission for this competency.
                                    // The key's existence in the weights object indicates permission.
                                    const isEditable = weights[comp.id] && Object.prototype.hasOwnProperty.call(weights[comp.id], e.id);

                                    return (
                                        <td key={e.id} className="px-6 py-4 whitespace-nowrap">
                                            {isEditable ? (
                                                <Input
                                                    type="number"
                                                    value={weights[comp.id]?.[e.id] ?? ''}
                                                    onChange={(ev) => handleWeightChange(comp.id, e.id, ev.target.value)}
                                                    className="w-24 text-center mx-auto"
                                                    min="0"
                                                    max="100"
                                                />
                                            ) : (
                                                <div className="w-24 h-10 flex items-center justify-center mx-auto border rounded-md bg-gray-100 text-gray-500 font-medium">
                                                    -
                                                </div>
                                            )}
                                        </td>
                                    );
                                })}
                                <td className={`px-6 py-4 whitespace-nowrap text-sm text-center font-semibold ${total === 100 ? 'text-green-600' : 'text-red-600'}`}>
                                    {total}%
                                </td>
                            </tr>
                        );
                    })}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default SourceWeightsPage;