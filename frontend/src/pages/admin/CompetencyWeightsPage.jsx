import React, {useEffect, useState} from 'react';
import {useSelector} from 'react-redux';
import PeriodCompetencyService from '../../services/periodCompetencyService';
import {Button} from '../../components/ui/button';
import {Input} from '../../components/ui/input';
import {toast} from 'react-toastify';
import {Alert, AlertDescription, AlertTitle} from '../../components/ui/alert';
import {Terminal} from 'lucide-react';

const CompetencyWeightsPage = () => {
    const [weights, setWeights] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);

    useEffect(() => {
        if (selectedPeriod) {
            fetchWeights();
        }
    }, [selectedPeriod]);

    const fetchWeights = async () => {
        setLoading(true);
        try {
            const response = await PeriodCompetencyService.getCompetencyWeights(selectedPeriod.id);
            const formattedWeights = response.data.map(w => ({ ...w, weight: Number(w.weight || 0) }));
            setWeights(formattedWeights);
        } catch (err) {
            setError('Yetkinlik ağırlıkları yüklenemedi.');
            toast.error('Yetkinlik ağırlıkları yüklenemedi.');
        } finally {
            setLoading(false);
        }
    };

    const handleWeightChange = (competencyId, value) => {
        const newWeight = parseInt(value, 10);
        if (isNaN(newWeight) && value !== '') return;

        const newWeights = weights.map(w =>
            w.competencyId === competencyId ? { ...w, weight: isNaN(newWeight) ? 0 : newWeight } : w
        );
        setWeights(newWeights);
    };

    const handleSave = async () => {
        const totalWeight = weights.reduce((acc, curr) => acc + curr.weight, 0);
        if (totalWeight !== 100) {
            toast.error(`Toplam ağırlık 100 olmalıdır. Mevcut toplam: ${totalWeight}%`);
            return;
        }

        try {
            const weightsToSave = {
                competencyWeights: weights.map(w => ({
                    competencyId: w.competencyId,
                    weight: w.weight
                }))
            };
            await PeriodCompetencyService.setCompetencyWeights(selectedPeriod.id, weightsToSave);
            toast.success('Ağırlıklar başarıyla kaydedildi.');
        } catch (err) {
            toast.error('Ağırlıklar kaydedilemedi.');
        }
    };

    const totalWeight = weights.reduce((acc, curr) => acc + curr.weight, 0);

    if (!selectedPeriod) {
        return <div className="p-8 text-center">Lütfen bir değerlendirme dönemi seçin.</div>;
    }

    if (loading) return <div className="p-8">Yükleniyor...</div>;
    if (error) return <div className="p-8 text-red-500">{error}</div>;

    return (
        <div>
            <h1 className="text-3xl font-bold my-6">Yetkinlik Ağırlıkları</h1>
            <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    <div className="md:col-span-1">
                        <h2 className="text-lg font-semibold">Ağırlık Yönetimi</h2>
                        <p className="text-gray-600 mt-2">
                            Değerlendirme içerisindeki yetkinliklerin ağırlıklarını belirleyebilirsiniz.
                            Tüm yetkinliklerin ağırlıkları toplamı 100 olmak zorundadır.
                        </p>
                    </div>
                    <div className="md:col-span-2 space-y-4">
                        {weights.length > 0 ? (
                            weights.map(w => (
                                <div key={w.competencyId} className="flex items-center justify-between">
                                    <label className="font-medium">{w.competencyTitle}</label>
                                    <Input
                                        type="number"
                                        value={w.weight.toString()}
                                        onChange={(e) => handleWeightChange(w.competencyId, e.target.value)}
                                        min="0"
                                        max="100"
                                        className="w-24 text-center"
                                    />
                                </div>
                            ))
                        ) : (
                            <p className="text-gray-500">Bu dönem için henüz yetkinlik eklenmemiş.</p>
                        )}

                        <hr className="my-4" />

                        <div className="flex items-center justify-between font-bold text-lg">
                            <span>Toplam Ağırlık</span>
                            <span className={totalWeight !== 100 ? 'text-red-500' : 'text-green-600'}>
                                {totalWeight}%
                            </span>
                        </div>

                        {totalWeight !== 100 && (
                             <Alert variant="destructive">
                                <Terminal className="h-4 w-4" />
                                <AlertTitle>Hata</AlertTitle>
                                <AlertDescription>
                                    Toplam ağırlık 100'e eşit olmalıdır.
                                </AlertDescription>
                            </Alert>
                        )}

                        <div className="flex justify-end mt-6">
                            <Button onClick={handleSave} disabled={totalWeight !== 100 || loading}>
                                Ağırlıkları Kaydet
                            </Button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CompetencyWeightsPage;
