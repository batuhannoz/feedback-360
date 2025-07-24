import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import EvaluationPeriodService from '../../services/evaluationPeriodService';
import { fetchPeriods } from '../../store/periodSlice';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';

const AddEvaluationPeriodModal = ({ isOpen, onClose }) => {
    const dispatch = useDispatch();
    const [evaluationName, setEvaluationName] = useState('');
    const [internalPeriodName, setInternalEvaluationName] = useState('');
    const [periodName, setPeriodName] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async () => {
        setError('');
        if (!evaluationName || !internalPeriodName || !periodName || !startDate || !endDate) {
            setError('Lütfen tüm alanları doldurun.');
            return;
        }

        const formattedStartDate = `${startDate}T00:00:00`;
        const formattedEndDate = `${endDate}T23:59:59`;

        const request = {
            evaluationName,
            periodName,
            internalPeriodName,
            startDate: formattedStartDate,
            endDate: formattedEndDate
        };

        try {
            await EvaluationPeriodService.createPeriod(request);
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
                    <DialogTitle>Değerlendirme Dönemi Oluştur</DialogTitle>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                    {/* Değerlendirme Adı */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="evaluationName" className="text-right">
                            Değerlendirme Adı
                        </Label>
                        <Input id="evaluationName" value={evaluationName} onChange={(e) => setEvaluationName(e.target.value)} className="col-span-3" />
                    </div>
                    {/* İç Değerlendirme Adı */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="internalName" className="text-right">
                            İç Değerlendirme Adı
                        </Label>
                        <Input id="internalName" value={internalPeriodName} onChange={(e) => setInternalEvaluationName(e.target.value)} className="col-span-3" />
                    </div>
                    {/* Değerlendirme Dönemi */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="periodName" className="text-right">
                            Değerlendirme Dönemi
                        </Label>
                        <Input id="periodName" value={periodName} onChange={(e) => setPeriodName(e.target.value)} className="col-span-3" />
                    </div>
                    {/* Başlangıç Tarihi */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="startDate" className="text-right">
                            Başlangıç Tarihi
                        </Label>
                        <Input id="startDate" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} className="col-span-3" />
                    </div>
                    {/* Bitiş Tarihi */}
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

export default AddEvaluationPeriodModal;