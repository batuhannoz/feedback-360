import React, { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '../ui/dialog';
import { XCircle } from 'lucide-react';
import { toast } from 'react-toastify';
import {Button} from "../ui/button.jsx";
import {Input} from "../ui/input.jsx";
import {Label} from "../ui/label.jsx";

const ScaleFormModal = ({ isOpen, onClose, onSave, initialData, mode }) => {
    const [scaleData, setScaleData] = useState({ name: '', options: [] });

    useEffect(() => {
        if (initialData) {
            setScaleData(JSON.parse(JSON.stringify(initialData)));
        }
    }, [initialData]);

    const handleNameChange = (e) => {
        setScaleData({ ...scaleData, name: e.target.value });
    };

    const handleOptionChange = (index, field, value) => {
        const newOptions = [...scaleData.options];
        newOptions[index][field] = field === 'score' ? parseInt(value) || 1 : value;
        setScaleData({ ...scaleData, options: newOptions });
    };

    const addOption = () => {
        if (scaleData.options.length >= 5) {
            toast.warn('En fazla 5 seçenek ekleyebilirsiniz.');
            return;
        }

        const usedScores = new Set(scaleData.options.map(o => o.score));
        let nextScore = -1;
        for (let i = 1; i <= 5; i++) {
            if (!usedScores.has(i)) {
                nextScore = i;
                break;
            }
        }

        if (nextScore === -1) {
            toast.warn('Tüm puanlar (1-5) zaten kullanılıyor.');
            return;
        }

        setScaleData({
            ...scaleData,
            options: [...scaleData.options, { score: nextScore, label: '' }],
        });
    };

    const removeOption = (index) => {
        const newOptions = scaleData.options.filter((_, i) => i !== index);
        setScaleData({ ...scaleData, options: newOptions });
    };

    const validateAndSave = () => {
        if (!scaleData.name.trim()) {
            toast.error('Ölçek adı boş olamaz.');
            return;
        }
        if (scaleData.options.length === 0) {
            toast.error('En az bir seçenek eklemelisiniz.');
            return;
        }

        const scores = new Set();
        for (const option of scaleData.options) {
            if (!option.label.trim()) {
                toast.error(`Puanı ${option.score} olan seçeneğin metni boş olamaz.`);
                return;
            }
            if (option.score < 1 || option.score > 5) {
                toast.error(`Puanlar 1 ile 5 arasında olmalıdır. Hatalı puan: ${option.score}`);
                return;
            }
            if (scores.has(option.score)) {
                toast.error(`${option.score} puanı birden fazla kullanılamaz.`);
                return;
            }
            scores.add(option.score);
        }

        const payload = {
            name: scaleData.name,
            options: scaleData.options.map(opt => ({ score: opt.score, label: opt.label }))
        };
        onSave(payload);
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[600px]">
                <DialogHeader>
                    <DialogTitle>{mode === 'create' ? 'Yeni Değerlendirme Ölçeği Oluştur' : 'Değerlendirme Ölçeğini Düzenle'}</DialogTitle>
                    <DialogDescription>
                        Ölçek adını ve 1'den 5'e kadar olan seçeneklerini buradan tanımlayabilirsiniz.
                    </DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="name" className="text-right">Ölçek Adı</Label>
                        <Input id="name" value={scaleData.name} onChange={handleNameChange} className="col-span-3" />
                    </div>

                    <h4 className="font-semibold mt-4">Seçenekler</h4>
                    <div className="space-y-2 max-h-60 overflow-y-auto pr-2">
                        {scaleData.options.map((option, index) => (
                            <div key={index} className="grid grid-cols-12 items-center my-3 gap-2">
                                <Label className="col-span-2 text-right">Puan</Label>
                                <Input
                                    type="number"
                                    value={option.score}
                                    onChange={(e) => handleOptionChange(index, 'score', e.target.value)}
                                    className="col-span-2"
                                    min="1"
                                    max="5"
                                />
                                <Label className="col-span-2 text-right">Metin</Label>
                                <Input
                                    value={option.label}
                                    onChange={(e) => handleOptionChange(index, 'label', e.target.value)}
                                    className="col-span-5"
                                />
                                <Button variant="ghost" size="sm" onClick={() => removeOption(index)} className="col-span-1">
                                    <XCircle className="h-4 w-4 text-red-500" />
                                </Button>
                            </div>
                        ))}
                    </div>
                    <Button
                        variant="outline"
                        onClick={addOption}
                        className="mt-2"
                        disabled={scaleData.options.length >= 5}
                    >
                        Seçenek Ekle
                    </Button>
                </div>
                <DialogFooter>
                    <Button variant="secondary" onClick={onClose}>İptal</Button>
                    <Button onClick={validateAndSave}>Kaydet</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default ScaleFormModal;
