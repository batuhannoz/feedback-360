import React, { useState, useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import  EvaluationPeriodService  from '../../services/evaluationPeriodService';
import { fetchPeriods } from '../../store/periodSlice';

const EditEvaluationPeriodModal = ({ isOpen, onClose, evaluationPeriod }) => {
    const dispatch = useDispatch();
    const [evaluationName, setEvaluationName] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        if (evaluationPeriod) {
            setEvaluationName(evaluationPeriod.evaluationName || '');
            setStartDate(evaluationPeriod.startDate ? new Date(evaluationPeriod.startDate).toISOString().split('T')[0] : '');
            setEndDate(evaluationPeriod.endDate ? new Date(evaluationPeriod.endDate).toISOString().split('T')[0] : '');
        }
    }, [evaluationPeriod]);

    const handleSubmit = async () => {
        setError('');
        if (!evaluationName || !startDate || !endDate) {
            setError('Lütfen tüm alanları doldurun.');
            return;
        }

        const request = {
            evaluationName,
            periodName: evaluationName, // Or derive as needed
            internalPeriodName: evaluationName.toLowerCase().replace(/\s+/g, '-'), // Or derive as needed
            startDate,
            endDate
        };

        try {
            await EvaluationPeriodService.updatePeriod(evaluationPeriod.id, request);
            dispatch(fetchPeriods());
            onClose();
        } catch (err) {
            setError(err.response?.data?.message || 'Bir hata oluştu.');
        }
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Değerlendirme Dönemini Düzenle</DialogTitle>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="name" className="text-right">
                            Değerlendirme Adı
                        </Label>
                        <Input id="name" value={evaluationName} onChange={(e) => setEvaluationName(e.target.value)} className="col-span-3" />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="startDate" className="text-right">
                            Başlangıç Tarihi
                        </Label>
                        <Input id="startDate" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} className="col-span-3" />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="endDate" className="text-right">
                            Bitiş Tarihi
                        </Label>
                        <Input id="endDate" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} className="col-span-3" />
                    </div>
                    {error && <p className="text-red-500 text-sm col-span-4 text-center">{error}</p>}
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>İptal</Button>
                    <Button onClick={handleSubmit}>Kaydet</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default EditEvaluationPeriodModal;
