import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import EvaluationPeriodService from '../../services/evaluationPeriodService';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { toast } from 'sonner';

const WeightsPage = () => {
    const { periodId } = useParams();
    const [weights, setWeights] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchWeights = async () => {
            try {
                const response = await EvaluationPeriodService.getCompetencyWeights(periodId);
                setWeights(response.data);
            } catch (err) {
                setError('Yetkinlik ağırlıkları yüklenemedi.');
                toast.error('Yetkinlik ağırlıkları yüklenemedi.');
            } finally {
                setLoading(false);
            }
        };

        fetchWeights();
    }, [periodId]);

    const handleWeightChange = (competencyId, value) => {
        const newWeights = weights.map(w => 
            w.id === competencyId ? { ...w, weight: value } : w
        );
        setWeights(newWeights);
    };

    const handleSave = async () => {
        const totalWeight = weights.reduce((acc, curr) => acc + Number(curr.weight || 0), 0);
        if (totalWeight !== 100) {
            toast.error('Toplam ağırlık 100 olmalıdır.');
            return;
        }

        try {
            const weightsToSave = {
                competencyWeights: weights.map(w => ({
                    competencyId: w.id,
                    weight: Number(w.weight)
                }))
            };
            await EvaluationPeriodService.setCompetencyWeights(periodId, weightsToSave);
            toast.success('Ağırlıklar başarıyla kaydedildi.');
        } catch (err) {
            toast.error('Ağırlıklar kaydedilemedi.');
        }
    };

    if (loading) return <div className="p-8">Yükleniyor...</div>;
    if (error) return <div className="p-8 text-red-500">{error}</div>;

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Yetkinlik Ağırlıkları</h1>
                <Button onClick={handleSave}>Kaydet</Button>
            </div>
            <div className="bg-white p-6 rounded-lg shadow-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Yetkinlik Adı</TableHead>
                            <TableHead className="w-[150px]">Ağırlık (%)</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {weights.map(w => (
                            <TableRow key={w.id}>
                                <TableCell>{w.competencyTitle}</TableCell>
                                <TableCell>
                                    <Input 
                                        type="number"
                                        value={w.weight || ''}
                                        onChange={(e) => handleWeightChange(w.id, e.target.value)}
                                        min="0"
                                        max="100"
                                    />
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
                <div className="text-right mt-4 font-bold">
                    Toplam: {weights.reduce((acc, curr) => acc + Number(curr.weight || 0), 0)}%
                </div>
            </div>
        </div>
    );
};

export default WeightsPage;

