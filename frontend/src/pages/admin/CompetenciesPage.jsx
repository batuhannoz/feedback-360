import React, { useState, useEffect, useCallback } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import PeriodCompetencyService from '../../services/periodCompetencyService';
import CompetencyQuestionService from '../../services/competencyQuestionService';
import { getEvaluatorsByPeriodId } from '../../services/periodEvaluatorService';
import { Button } from '../../components/ui/button';
import { Plus, Edit, Trash2, ChevronDown, ChevronRight, Settings } from 'lucide-react';
import { toast } from 'react-toastify';
import { cn } from "../../lib/utils.js";

import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '../../components/ui/dialog';
import { Input } from '../../components/ui/input';
import { Textarea } from '../../components/ui/textarea.jsx';
import { Label } from '../../components/ui/label';
import { Checkbox } from '../../components/ui/checkbox';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import EvaluationScaleService from "../../services/scaleOptionsService.js";

const evaluatorTypeTranslations = {
    MANAGER: 'Yönetici',
    SUBORDINATE: 'Ast',
    PEER: 'Eş Değer',
    SELF: 'Kendisi',
    OTHER: 'Diğer'
};

const CompetenciesPage = () => {
    const [competencies, setCompetencies] = useState([]);
    const [allEvaluators, setAllEvaluators] = useState([]);
    const [evaluationScales, setEvaluationScales] = useState([]);
    const [loading, setLoading] = useState(false);
    const [expandedCompetencies, setExpandedCompetencies] = useState(new Set());
    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);

    const [isCompetencyModalOpen, setIsCompetencyModalOpen] = useState(false);
    const [isQuestionModalOpen, setIsQuestionModalOpen] = useState(false);

    const [currentCompetency, setCurrentCompetency] = useState(null);
    const [currentQuestion, setCurrentQuestion] = useState(null);
    const navigate = useNavigate();

    const fetchInitialData = useCallback(async () => {
        if (!selectedPeriod) return;
        setLoading(true);
        try {
            const [evaluatorsRes, competenciesRes, scalesRes] = await Promise.all([
                getEvaluatorsByPeriodId(selectedPeriod.id),
                PeriodCompetencyService.getCompetenciesByPeriod(selectedPeriod.id),
                EvaluationScaleService.getScales()
            ]);

            const formattedEvaluators = evaluatorsRes.data.map(e => ({
                ...e,
                name: e.name || evaluatorTypeTranslations[e.evaluatorType] || ''
            }));
            setAllEvaluators(formattedEvaluators);
            setEvaluationScales(scalesRes.data);

            const competenciesWithData = await Promise.all(competenciesRes.data.map(async (comp) => {
                const permissionsRes = await PeriodCompetencyService.getCompetencyEvaluatorPermissions(selectedPeriod.id, comp.id);
                const assignedEvaluatorIds = new Set(permissionsRes.data.map(p => p.evaluatorId));

                const questionsRes = await CompetencyQuestionService.getQuestionsForCompetency(selectedPeriod.id, comp.id);

                return {
                    ...comp,
                    questions: questionsRes.data || [],
                    assignedEvaluatorIds: assignedEvaluatorIds
                };
            }));

            setCompetencies(competenciesWithData);
        } catch (error) {
            toast.error('Veriler getirilirken bir hata oluştu.');
            console.error(error);
        }
        setLoading(false);
    }, [selectedPeriod]);


    useEffect(() => {
        fetchInitialData();
    }, [fetchInitialData]);


    const handleToggleCompetency = (competencyId) => {
        setExpandedCompetencies(prevExpanded => {
            const newExpanded = new Set(prevExpanded);
            if (newExpanded.has(competencyId)) {
                newExpanded.delete(competencyId);
            } else {
                newExpanded.add(competencyId);
            }
            return newExpanded;
        });
    };

    const handleToggleEvaluator = async (competencyId, evaluatorId) => {
        const competency = competencies.find(c => c.id === competencyId);
        if (!competency) return;

        const newAssignedIds = new Set(competency.assignedEvaluatorIds);
        if (newAssignedIds.has(evaluatorId)) {
            newAssignedIds.delete(evaluatorId);
        } else {
            newAssignedIds.add(evaluatorId);
        }

        const optimisticUpdate = competencies.map(c => c.id === competencyId ? { ...c, assignedEvaluatorIds: newAssignedIds } : c);
        setCompetencies(optimisticUpdate);

        try {
            await PeriodCompetencyService.assignEvaluatorsToCompetency(selectedPeriod.id, competencyId, { evaluatorIds: Array.from(newAssignedIds) });
            toast.success(`Yetki güncellendi.`);
        } catch (error) {
            toast.error('Atama kaydedilirken bir hata oluştu.');
            fetchInitialData();
        }
    };

    const handleOpenCompetencyModal = (competency = null) => {
        setCurrentCompetency(competency);
        setIsCompetencyModalOpen(true);
    };

    const handleSaveCompetency = async (competencyData) => {
        try {
            if (currentCompetency) {
                toast.info('Update competency not implemented yet.');
            } else {
                await PeriodCompetencyService.addCompetencyToPeriod(selectedPeriod.id, { title: competencyData.title });
                toast.success('Yetkinlik başarıyla eklendi.');
            }
            fetchInitialData();
            setIsCompetencyModalOpen(false);
        } catch (error) {
            toast.error('Yetkinlik kaydedilirken bir hata oluştu.');
        }
    };

    const handleDeleteCompetency = async (competencyId) => {
        if (window.confirm('Bu yetkinliği silmek istediğinizden emin misiniz? Bu işleme bağlı tüm sorular da silinecektir.')) {
            try {
                await PeriodCompetencyService.deleteCompetencyFromPeriod(selectedPeriod.id, competencyId);
                toast.success('Yetkinlik başarıyla silindi.');
                fetchInitialData();
            } catch (error) {
                toast.error('Yetkinlik silinirken bir hata oluştu.');
            }
        }
    };

    const handleOpenQuestionModal = (competency, question = null) => {
        setCurrentCompetency(competency);
        setCurrentQuestion(question);
        setIsQuestionModalOpen(true);
    };

    const handleSaveQuestion = async (questionData) => {
        const request = {
            questionText: questionData.questionText,
            evaluationScaleId: questionData.evaluationScaleId,
            hiddenScores: Array.from(questionData.hiddenScores),
            scoresRequiringComment: Array.from(questionData.scoresRequiringComment),
        };

        try {
            if (currentQuestion) {
                await CompetencyQuestionService.updateQuestion(selectedPeriod.id, currentCompetency.id, currentQuestion.id, request);
                toast.success('Soru başarıyla güncellendi.');
            } else {
                await CompetencyQuestionService.addQuestionToCompetency(selectedPeriod.id, currentCompetency.id, request);
                toast.success('Soru başarıyla eklendi.');
            }
            fetchInitialData();
            setIsQuestionModalOpen(false);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Soru kaydedilirken bir hata oluştu.');
        }
    };

    const handleDeleteQuestion = async (competencyId, questionId) => {
        if (window.confirm('Bu soruyu silmek istediğinizden emin misiniz?')) {
            try {
                await CompetencyQuestionService.deleteQuestion(selectedPeriod.id, competencyId, questionId);
                toast.success('Soru başarıyla silindi.');
                fetchInitialData();
            } catch (error) {
                toast.error('Soru silinirken bir hata oluştu.');
            }
        }
    };

    if (!selectedPeriod) {
        return <div className="p-8 text-center">Lütfen bir değerlendirme dönemi seçin.</div>;
    }

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <div className="flex justify-between items-center mb-6">
                <div className="flex items-center gap-4">
                    <h1 className="text-3xl font-bold">Yetkinlikler</h1>
                    <Button variant="outline" size="icon" onClick={() => navigate(`/dashboard/competency/settings`)}>
                        <Settings className="h-5 w-5" />
                    </Button>
                </div>
                <Button onClick={() => handleOpenCompetencyModal()}>
                    <Plus className="mr-2 h-4 w-4" /> Yetkinlik Ekle
                </Button>
            </div>

            <div className="bg-white p-6 rounded-lg shadow-md">
                {loading ? (
                    <p>Yükleniyor...</p>
                ) : (
                    <div className="space-y-4">
                        {competencies.map(competency => (
                            <div key={competency.id} className="border rounded-lg">
                                <div
                                    className="flex items-center justify-between p-4 cursor-pointer bg-gray-50 hover:bg-gray-100"
                                    onClick={() => handleToggleCompetency(competency.id)}
                                >
                                    <div className="flex items-center">
                                        {expandedCompetencies.has(competency.id) ?
                                            <ChevronDown className="h-5 w-5 mr-2" /> :
                                            <ChevronRight className="h-5 w-5 mr-2" />}
                                        <h2 className="font-semibold text-lg">{competency.title}</h2>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        {allEvaluators.map(evaluator => {
                                            const hasAccess = competency.assignedEvaluatorIds.has(evaluator.id);
                                            return (
                                                <Button
                                                    key={evaluator.id}
                                                    variant={hasAccess ? "default" : "outline"}
                                                    size="sm"
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        handleToggleEvaluator(competency.id, evaluator.id);
                                                    }}
                                                    className={cn("text-xs h-7", { "bg-black text-white": hasAccess })}
                                                >
                                                    {evaluator.name}
                                                </Button>
                                            );
                                        })}
                                        <Button variant="outline" size="sm" onClick={(e) => {
                                            e.stopPropagation();
                                            handleOpenCompetencyModal(competency);
                                        }}><Edit className="h-4 w-4" /></Button>
                                        <Button variant="destructive" size="sm" onClick={(e) => {
                                            e.stopPropagation();
                                            handleDeleteCompetency(competency.id);
                                        }}><Trash2 className="h-4 w-4" /></Button>
                                    </div>
                                </div>
                                {expandedCompetencies.has(competency.id) && (
                                    <div className="p-4 border-t space-y-4">
                                        <div>
                                            <div className="flex justify-between items-center mb-2">
                                                <h3 className="font-semibold">Sorular</h3>
                                                <Button size="sm" onClick={() => handleOpenQuestionModal(competency)}>
                                                    <Plus className="mr-2 h-4 w-4" /> Soru Ekle
                                                </Button>
                                            </div>
                                            <table className="w-full text-sm text-left">
                                                <tbody>
                                                {competency.questions.map(question => (
                                                    <tr key={question.id} className="border-b">
                                                        <td className="p-2">{question.questionText}</td>
                                                        <td className="p-2 w-24 text-right">
                                                            <div className="flex gap-2 justify-end">
                                                                <Button variant="outline" size="icon"
                                                                        className="h-8 w-8"
                                                                        onClick={() => handleOpenQuestionModal(competency, question)}><Edit
                                                                    className="h-4 w-4" /></Button>
                                                                <Button variant="destructive" size="icon"
                                                                        className="h-8 w-8"
                                                                        onClick={() => handleDeleteQuestion(competency.id, question.id)}><Trash2
                                                                    className="h-4 w-4" /></Button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                ))}
                                                {competency.questions.length === 0 && (
                                                    <tr>
                                                        <td colSpan="2" className="p-4 text-center text-gray-500">Bu
                                                            yetkinlik için henüz soru eklenmemiş.
                                                        </td>
                                                    </tr>
                                                )}
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <CompetencyModal
                isOpen={isCompetencyModalOpen}
                onClose={() => setIsCompetencyModalOpen(false)}
                onSave={handleSaveCompetency}
                competency={currentCompetency}
            />

            {isQuestionModalOpen && (
                <QuestionModal
                    isOpen={isQuestionModalOpen}
                    onClose={() => setIsQuestionModalOpen(false)}
                    onSave={handleSaveQuestion}
                    question={currentQuestion}
                    evaluationScales={evaluationScales}
                />
            )}
        </div>
    );
};

const CompetencyModal = ({ isOpen, onClose, onSave, competency }) => {
    const [title, setTitle] = useState('');

    useEffect(() => {
        setTitle(competency ? competency.title : '');
    }, [competency]);

    const handleSave = () => {
        onSave({ title });
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{competency ? 'Yetkinliği Düzenle' : 'Yeni Yetkinlik Ekle'}</DialogTitle>
                </DialogHeader>
                <div className="py-4">
                    <Input
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        placeholder="Yetkinlik Adı"
                    />
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>İptal</Button>
                    <Button onClick={handleSave}>Kaydet</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

const QuestionModal = ({ isOpen, onClose, onSave, question, evaluationScales }) => {
    const [questionText, setQuestionText] = useState('');
    const [evaluationScaleId, setEvaluationScaleId] = useState(null);
    const [scoresRequiringComment, setScoresRequiringComment] = useState(new Set());
    const [hiddenScores, setHiddenScores] = useState(new Set());

    const scoreOptions = evaluationScales.find(s => s.id === evaluationScaleId)?.options || [];

    useEffect(() => {
        if (question) {
            setQuestionText(question.questionText || '');
            const scaleId = question.scaleOptions?.[0] ? evaluationScales.find(s => s.name === question.scaleOptions[0].scaleName)?.id : null;
            setEvaluationScaleId(question.evaluationScaleId || scaleId);
            setScoresRequiringComment(new Set(question.scoresRequiringComment || []));
            setHiddenScores(new Set(question.hiddenScores || []));
        } else {
            setQuestionText('');
            setEvaluationScaleId(evaluationScales.length > 0 ? evaluationScales[0].id : null);
            setScoresRequiringComment(new Set());
            setHiddenScores(new Set());
        }
    }, [question, evaluationScales]);

    const handleSave = () => {
        if (!evaluationScaleId) {
            toast.error("Lütfen bir değerlendirme ölçeği seçin.");
            return;
        }
        onSave({ questionText, evaluationScaleId, scoresRequiringComment, hiddenScores });
    };

    const handleCheckboxChange = (score, type, checked) => {
        const setter = type === 'comment' ? setScoresRequiringComment : setHiddenScores;
        setter(prev => {
            const newSet = new Set(prev);
            if (checked) {
                newSet.add(score);
            } else {
                newSet.delete(score);
            }
            return newSet;
        });
    };

    const handleScaleChange = (scaleId) => {
        setEvaluationScaleId(parseInt(scaleId));
        setScoresRequiringComment(new Set());
        setHiddenScores(new Set());
    }

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="max-w-4xl">
                <DialogHeader>
                    <DialogTitle>{question ? 'Soruyu Düzenle' : 'Yeni Soru Ekle'}</DialogTitle>
                </DialogHeader>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8 py-4">
                    <div>
                        <div className="space-y-2">
                            <Label htmlFor="evaluation-scale">Değerlendirme Ölçeği</Label>
                            <Select
                                value={evaluationScaleId?.toString()}
                                onValueChange={handleScaleChange}
                            >
                                <SelectTrigger id="evaluation-scale">
                                    <SelectValue placeholder="Bir ölçek seçin..." />
                                </SelectTrigger>
                                <SelectContent>
                                    {evaluationScales.map(scale => (
                                        <SelectItem key={scale.id} value={scale.id.toString()}>
                                            {scale.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                        <div className="mt-4 space-y-2">
                            <Label htmlFor="question-text">Soru Metni</Label>
                            <Textarea
                                id="question-text"
                                value={questionText}
                                onChange={(e) => setQuestionText(e.target.value)}
                                placeholder="Soru metnini buraya girin..."
                                rows={10}
                            />
                        </div>
                    </div>
                    <div className="space-y-4">
                        <Label>Seçenek Ayarları</Label>
                        {evaluationScaleId ? (
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Puan</TableHead>
                                        <TableHead>Metin</TableHead>
                                        <TableHead className="text-center">Zorunlu Yorum</TableHead>
                                        <TableHead className="text-center">Gizle</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {scoreOptions.map(({ score, label }) => (
                                        <TableRow key={score}>
                                            <TableCell className="font-medium">{score}</TableCell>
                                            <TableCell>{label}</TableCell>
                                            <TableCell className="text-center">
                                                <Checkbox
                                                    checked={scoresRequiringComment.has(score)}
                                                    onCheckedChange={(checked) => handleCheckboxChange(score, 'comment', checked)}
                                                />
                                            </TableCell>
                                            <TableCell className="text-center">
                                                <Checkbox
                                                    checked={hiddenScores.has(score)}
                                                    onCheckedChange={(checked) => handleCheckboxChange(score, 'hide', checked)}
                                                />
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        ) : (
                            <div className="flex items-center justify-center h-full bg-gray-50 rounded-md">
                                <p className="text-gray-500">Ayarları görmek için bir ölçek seçin.</p>
                            </div>
                        )}
                    </div>
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>İptal</Button>
                    <Button onClick={handleSave}>Kaydet</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default CompetenciesPage;