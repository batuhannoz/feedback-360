import React, {useState, useEffect} from 'react';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import {useSelector, useDispatch} from 'react-redux';
import {useNavigate} from 'react-router-dom';
import {Button} from '../../components/ui/button';
import {Input} from '../../components/ui/input';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '../../components/ui/select';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '../../components/ui/table';
import {Checkbox} from '../../components/ui/checkbox';
import {Plus, Edit, BarChart2, Play, Mail, XCircle, Trash2, CheckCircle} from 'lucide-react';
import {fetchPeriods, setSelectedPeriod} from '../../store/periodSlice';
import AddEvaluationPeriodModal from '../../components/admin/AddEvaluationPeriodModal';
import EditEvaluationPeriodModal from '../../components/admin/EditEvaluationPeriodModal';
import ConfirmationDialog from '../../components/ui/ConfirmationDialog';
import EvaluationPeriodService from '../../services/evaluationPeriodService';


const getStatusText = (status) => {
    switch (status) {
        case 'NOT_STARTED':
            return 'Taslak';
        case 'IN_PROGRESS':
            return 'Devam Ediyor';
        case 'COMPLETED':
            return 'Tamamlandı';
        case 'CANCELLED':
            return 'İptal Edildi';
        default:
            return 'Bilinmiyor';
    }
};

const EvaluationsPage = () => {
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [selectedEvaluation, setSelectedEvaluation] = useState(null);
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [isStartConfirmOpen, setIsStartConfirmOpen] = useState(false);
    const [isFinishConfirmOpen, setIsFinishConfirmOpen] = useState(false);
    const [isCancelConfirmOpen, setIsCancelConfirmOpen] = useState(false);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const {periods: evaluations, loading, error} = useSelector((state) => state.period);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedEvaluations, setSelectedEvaluations] = useState([]);

    useEffect(() => {
        dispatch(fetchPeriods());
    }, [dispatch]);

    const handleSelectAll = (checked) => {
        if (checked) {
            setSelectedEvaluations(evaluations.map(e => e.id));
        } else {
            setSelectedEvaluations([]);
        }
    };

    const handleEditClick = () => {
        if (selectedEvaluations.length === 1) {
            const evaluationToEdit = evaluations.find(e => e.id === selectedEvaluations[0]);
            setSelectedEvaluation(evaluationToEdit);
            setIsEditModalOpen(true);
        }
    };

    const handleDeleteClick = () => {
        if (selectedEvaluations.length > 0) {
            setIsConfirmModalOpen(true);
        }
    };

    const handleDeleteConfirm = async () => {
        try {
            await Promise.all(selectedEvaluations.map(id => EvaluationPeriodService.deletePeriod(id)));
            dispatch(fetchPeriods());
            setSelectedEvaluations([]);
        } catch (error) {
            console.error('Failed to delete evaluation periods', error);
            // You can add user-facing error handling here
        }
        setIsConfirmModalOpen(false);
    };

    const handleStartClick = () => {
        if (!isStartButtonDisabled) {
            setIsStartConfirmOpen(true);
        }
    };

    const handleStartConfirm = async () => {
        try {
            await Promise.all(selectedEvaluations.map(id => EvaluationPeriodService.updatePeriodStatus(id, {status: 'IN_PROGRESS'})));
            dispatch(fetchPeriods());
            setSelectedEvaluations([]);
        } catch (error) {
            console.error('Failed to start evaluation periods', error);
        }
        setIsStartConfirmOpen(false);
    };

    const handleFinishClick = () => {
        if (!isFinishButtonDisabled) {
            setIsFinishConfirmOpen(true);
        }
    };

    const handleFinishConfirm = async () => {
        try {
            await Promise.all(selectedEvaluations.map(id => EvaluationPeriodService.updatePeriodStatus(id, {status: 'COMPLETED'})));
            dispatch(fetchPeriods());
            setSelectedEvaluations([]);
        } catch (error) {
            console.error('Failed to finish evaluation periods', error);
        }
        setIsFinishConfirmOpen(false);
    };

    const handleCancelClick = () => {
        if (!isCancelButtonDisabled) {
            setIsCancelConfirmOpen(true);
        }
    };

    const handleCancelConfirm = async () => {
        try {
            await Promise.all(selectedEvaluations.map(id => EvaluationPeriodService.updatePeriodStatus(id, {status: 'CANCELLED'})));
            dispatch(fetchPeriods());
            setSelectedEvaluations([]);
        } catch (error) {
            console.error('Failed to cancel evaluation periods', error);
        }
        setIsCancelConfirmOpen(false);
    };

    const handleSelectOne = (id) => {
        if (selectedEvaluations.includes(id)) {
            setSelectedEvaluations(selectedEvaluations.filter(selectedId => selectedId !== id));
        } else {
            setSelectedEvaluations([...selectedEvaluations, id]);
        }
    };

    const isAllSelected = evaluations.length > 0 && selectedEvaluations.length === evaluations.length;

    const isStartButtonDisabled = selectedEvaluations.length === 0 || selectedEvaluations.some(id => {
        const evalPeriod = evaluations.find(e => e.id === id);
        return evalPeriod && evalPeriod.status !== 'NOT_STARTED';
    });

    const isFinishButtonDisabled = selectedEvaluations.length === 0 || selectedEvaluations.some(id => {
        const evalPeriod = evaluations.find(e => e.id === id);
        return evalPeriod && evalPeriod.status !== 'IN_PROGRESS';
    });

    const isCancelButtonDisabled = selectedEvaluations.length === 0 || selectedEvaluations.some(id => {
        const evalPeriod = evaluations.find(e => e.id === id);
        return evalPeriod && (evalPeriod.status === 'COMPLETED' || evalPeriod.status === 'CANCELLED');
    });

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Değerlendirmeler</h1>
                <Button onClick={() => setIsModalOpen(true)}>
                    <Plus className="mr-2 h-4 w-4"/> Değerlendirme Oluştur
                </Button>
            </div>

            <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-4">
                        <Input placeholder="Değerlendirmeleri Filtrele" className="w-64"/>
                        <Select defaultValue="all">
                            <SelectTrigger className="w-48">
                                <SelectValue placeholder="Tüm Dönemler"/>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="all">Tüm Dönemler</SelectItem>
                                <SelectItem value="active">Aktif</SelectItem>
                                <SelectItem value="completed">Tamamlanmış</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    {selectedEvaluations.length > 0 && (
                        <div className="flex items-center gap-2">
                            <Button variant="outline" size="sm" onClick={handleEditClick}
                                    disabled={selectedEvaluations.length !== 1}><Edit className="mr-2 h-4 w-4"/>Düzenle</Button>
                            <Button variant="outline" size="sm" onClick={handleStartClick}
                                    disabled={isStartButtonDisabled}><Play className="mr-2 h-4 w-4"/>Başlat</Button>
                            <Button variant="outline" size="sm"><Mail className="mr-2 h-4 w-4"/>E-Posta</Button>
                            <Button variant="outline" size="sm" onClick={handleCancelClick}
                                    disabled={isCancelButtonDisabled}><XCircle className="mr-2 h-4 w-4"/>İptal
                                Et</Button>
                            <Button variant="destructive" size="sm" onClick={handleDeleteClick}
                                    disabled={selectedEvaluations.length === 0}><Trash2
                                className="mr-2 h-4 w-4"/>Sil</Button>
                            <Button variant="destructive" size="sm" onClick={handleFinishClick}
                                    disabled={isFinishButtonDisabled}><CheckCircle
                                className="mr-2 h-4 w-4"/>Bitir</Button>
                        </div>
                    )}
                </div>

                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[50px]">
                                <Checkbox
                                    checked={isAllSelected}
                                    onCheckedChange={handleSelectAll}
                                />
                            </TableHead>
                            <TableHead>Değerlendirme Adı</TableHead>
                            <TableHead>Başlangıç ve Bitiş Tarihi</TableHead>
                            <TableHead>Durum</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {loading && <TableRow><TableCell colSpan="5" className="text-center"><LoadingSpinner /></TableCell></TableRow>}
                        {error && <TableRow><TableCell colSpan="5"
                                                       className="text-center text-red-500">{error}</TableCell></TableRow>}
                        {!loading && !error && evaluations.length === 0 && (
                            <TableRow>
                                <TableCell colSpan="5" className="text-center py-12">
                                    <h2 className="text-xl font-semibold mb-2">Hiç Değerlendirme Bulunamadı</h2>
                                    <p className="text-gray-500 mb-4">Hadi ilk değerlendirmenizi oluşturun.</p>
                                    <Button onClick={() => setIsModalOpen(true)}>
                                        <Plus className="mr-2 h-4 w-4"/> Değerlendirme Oluştur
                                    </Button>
                                </TableCell>
                            </TableRow>
                        )}
                        {!loading && !error && evaluations.map((evaluation) => (
                            <TableRow key={evaluation.id} onClick={() => navigate(`/dashboard/evaluations/${evaluation.id}`)}>
                                <TableCell onClick={(e) => e.stopPropagation()}>
                                    <Checkbox
                                        checked={selectedEvaluations.includes(evaluation.id)}
                                        onCheckedChange={() => handleSelectOne(evaluation.id)}
                                    />
                                </TableCell>
                                <TableCell>{evaluation.periodName}</TableCell>
                                <TableCell>{new Date(evaluation.startDate).toLocaleDateString()} - {new Date(evaluation.endDate).toLocaleDateString()}</TableCell>
                                <TableCell>{getStatusText(evaluation.status)}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>

            <EditEvaluationPeriodModal
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)}
                evaluationPeriod={selectedEvaluation}
            />

            <ConfirmationDialog
                isOpen={isConfirmModalOpen}
                onClose={() => setIsConfirmModalOpen(false)}
                onConfirm={handleDeleteConfirm}
                title="Değerlendirmeleri Sil"
                description={`Seçili ${selectedEvaluations.length} değerlendirmeyi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.`}
            />

            <ConfirmationDialog
                isOpen={isStartConfirmOpen}
                onClose={() => setIsStartConfirmOpen(false)}
                onConfirm={handleStartConfirm}
                title="Değerlendirmeleri Başlat"
                description={`Seçili ${selectedEvaluations.length} değerlendirmeyi başlatmak istediğinizden emin misiniz?`}
            />

            <ConfirmationDialog
                isOpen={isFinishConfirmOpen}
                onClose={() => setIsFinishConfirmOpen(false)}
                onConfirm={handleFinishConfirm}
                title="Değerlendirmeleri Bitir"
                description={`Seçili ${selectedEvaluations.length} değerlendirmeyi bitirmek istediğinizden emin misiniz?`}
            />

            <ConfirmationDialog
                isOpen={isCancelConfirmOpen}
                onClose={() => setIsCancelConfirmOpen(false)}
                onConfirm={handleCancelConfirm}
                title="Değerlendirmeleri İptal Et"
                description={`Seçili ${selectedEvaluations.length} değerlendirmeyi iptal etmek istediğinizden emin misiniz?`}
            />

            <AddEvaluationPeriodModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSuccess={(newPeriod) => {
                    dispatch(fetchPeriods());
                    dispatch(setSelectedPeriod(newPeriod));
                    setIsModalOpen(false);
                    navigate('/dashboard/evaluators');
                }}
            />
        </div>
    );
};

export default EvaluationsPage;
