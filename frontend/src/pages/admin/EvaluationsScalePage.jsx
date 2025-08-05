import React, { useState, useEffect, useCallback } from 'react';
import { Button } from '../../components/ui/button';
import { toast } from 'react-toastify';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/card';
import { PlusCircle, Edit, Trash2, Terminal } from 'lucide-react';
import EvaluationScaleService from '../../services/scaleOptionsService.js';
import ScaleFormModal from '../../components/admin/ScaleFormModal.jsx';

const EvaluationScalePage = () => {
    const [scales, setScales] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('create');
    const [currentScale, setCurrentScale] = useState(null);

    const fetchScales = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await EvaluationScaleService.getScales();
            setScales(response.data);
        } catch (err) {
            const message = 'Değerlendirme ölçekleri yüklenemedi.';
            setError(message);
            toast.error(message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchScales();
    }, [fetchScales]);

    const handleAddNew = () => {
        setCurrentScale({ name: '', options: [{ score: 1, label: '' }] });
        setModalMode('create');
        setIsModalOpen(true);
    };

    const handleEdit = (scale) => {
        setCurrentScale(scale);
        setModalMode('edit');
        setIsModalOpen(true);
    };

    const handleDelete = async (scaleId) => {
        if (window.confirm('Bu ölçeği silmek istediğinizden emin misiniz? Bu ölçeği kullanan sorular varsa işlem başarısız olacaktır.')) {
            try {
                await EvaluationScaleService.deleteScale(scaleId);
                toast.success('Ölçek başarıyla silindi.');
                fetchScales();
            } catch (err) {
                toast.error(err.response?.data?.message || 'Ölçek silinemedi.');
            }
        }
    };

    const handleSave = async (scaleData) => {
        try {
            if (modalMode === 'create') {
                await EvaluationScaleService.createScale(scaleData);
                toast.success('Yeni ölçek başarıyla oluşturuldu.');
            } else {
                await EvaluationScaleService.updateScale(currentScale.id, scaleData);
                toast.success('Ölçek başarıyla güncellendi.');
            }
            setIsModalOpen(false);
            fetchScales(); // Listeyi yenile
        } catch (err) {
            toast.error(err.response?.data?.message || 'İşlem başarısız oldu.');
        }
    };

    if (loading) return <div className="p-8">Yükleniyor...</div>;
    if (error) return (
        <Alert variant="destructive" className="m-8">
            <Terminal className="h-4 w-4" />
            <AlertTitle>Hata</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
        </Alert>
    );

    return (
        <div>
            <h1 className="text-3xl font-bold my-6">Değerlendirme Ölçekleri</h1>
            <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="flex justify-between items-center mb-4">
                    <div>
                        <h2 className="text-lg font-semibold">Ölçek Yönetimi</h2>
                        <p className="text-gray-600 mt-2">
                            Sorularda kullanılacak değerlendirme ölçeklerini buradan yönetebilirsiniz.
                        </p>
                    </div>
                    <Button onClick={handleAddNew}>
                        <PlusCircle className="mr-2 h-4 w-4" /> Yeni Ölçek Ekle
                    </Button>
                </div>

                <div className="space-y-4">
                    {scales.length > 0 ? (
                        scales.map(scale => (
                            <Card key={scale.id}>
                                <CardHeader className="flex flex-row items-center justify-between">
                                    <div >
                                        <CardTitle>{scale.name}</CardTitle>
                                        <CardDescription className="pt-2">
                                            {scale.options.map(opt => `${opt.score}: ${opt.label}`).join(' / ')}
                                        </CardDescription>
                                    </div>
                                    <div className="flex space-x-2">
                                        <Button variant="outline" size="sm" onClick={() => handleEdit(scale)}>
                                            <Edit className="h-4 w-4 mr-2"/> Düzenle
                                        </Button>
                                        <Button variant="destructive" size="sm" onClick={() => handleDelete(scale.id)}>
                                            <Trash2 className="h-4 w-4 mr-2" /> Sil
                                        </Button>
                                    </div>
                                </CardHeader>
                            </Card>
                        ))
                    ) : (
                        <p className="text-gray-500 text-center py-8">Henüz bir değerlendirme ölçeği oluşturulmamış.</p>
                    )}
                </div>
            </div>

            {isModalOpen && (
                <ScaleFormModal
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    onSave={handleSave}
                    initialData={currentScale}
                    mode={modalMode}
                />
            )}
        </div>
    );
};

export default EvaluationScalePage;
