import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { getEvaluatorsByPeriodId, setEvaluatorsByPeriodId } from '../../services/periodEvaluatorService';
import { toast } from 'react-toastify';

const EvaluatorsPage = () => {
    const [evaluators, setEvaluators] = useState([]);
    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);

    useEffect(() => {
        if (selectedPeriod) {
            getEvaluatorsByPeriodId(selectedPeriod.id)
                .then(response => {
                    setEvaluators(response.data);
                })
                .catch(error => {
                    toast.error('Değerlendiriciler getirilirken bir hata oluştu.');
                });
        }
    }, [selectedPeriod]);

    const handleNameChange = (id, newName) => {
        setEvaluators(evaluators.map(evaluator =>
            evaluator.id === id ? { ...evaluator, name: newName } : evaluator
        ));
    };

    const handleSave = () => {
        const updateRequest = evaluators.map(e => ({ id: e.id, name: e.name, type: e.type }));
        setEvaluatorsByPeriodId(selectedPeriod.id, updateRequest)
            .then(() => {
                toast.success('Kaynak isimleri başarıyla güncellendi.');
            })
            .catch(() => {
                toast.error('Kaynak isimleri güncellenirken bir hata oluştu.');
            });
    };

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">Kaynaklar</h1>
            <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div className="md:col-span-1">
                        <h2 className="text-lg font-semibold">Kaynak İsimleri</h2>
                        <p className="text-gray-600 mt-2">
                            Değerlendirme raporlarında yer alan kaynak isimlerini güncelleyebilirsiniz.
                        </p>
                    </div>
                    <div className="md:col-span-2">
                        {
                            evaluators.map((evaluator) => (
                                <div key={evaluator.id} className="mb-4">
                                    <input
                                        type="text"
                                        value={evaluator.name}
                                        onChange={(e) => handleNameChange(evaluator.id, e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md"
                                    />
                                </div>
                            ))
                        }
                        <div className="flex justify-end mt-6">
                            <button
                                onClick={handleSave}
                                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
                            >
                                Kaydet
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default EvaluatorsPage;
