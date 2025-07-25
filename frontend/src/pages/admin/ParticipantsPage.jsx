import React, {useState, useEffect, useMemo, useCallback} from 'react';
import {DragDropContext, Droppable, Draggable} from 'react-beautiful-dnd';
import {useSelector} from 'react-redux';
import {getUsers} from '../../services/userService';
import {
    getParticipantsByPeriod,
    addParticipantToPeriod,
    deleteParticipantFromPeriod,
    getParticipantAssignments,
    saveParticipantAssignments
} from '../../services/participantService';
import {getEvaluatorsByPeriodId} from '../../services/periodEvaluatorService';
import {Input} from '../../components/ui/input';
import {Button} from '../../components/ui/button';
import {toast} from 'sonner';
import {StrictModeDroppable} from "../../components/StrictModeDroppable.tsx";

const evaluatorTypeTranslations = {
    MANAGER: 'Müdür',
    SUBORDINATE: 'Ast',
    PEER: 'Akran',
    SELF: 'Kendisi',
    OTHER: 'Diğer'
};

const ParticipantsPage = () => {
    const [step, setStep] = useState(1);
    const [allCompanyUsers, setAllCompanyUsers] = useState([]);
    const [participants, setParticipants] = useState([]);
    const [availableUsers, setAvailableUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);
    const [evaluatorTypes, setEvaluatorTypes] = useState([]);
    const [selectedParticipant, setSelectedParticipant] = useState(null);
    const [assignmentSearchTerm, setAssignmentSearchTerm] = useState('');
    const [isSelfEvaluation, setIsSelfEvaluation] = useState(false);

    const [dndState, setDndState] = useState({users: {}, columns: {}, columnOrder: []});

    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);

    const initializeDndState = useCallback((evalTypes, companyUsers, existingAssignments = [], currentParticipant) => {
        const users = {};
        companyUsers.forEach(u => {
            users[u.id] = u;
        });

        const columns = {};
        const assignedUserIds = new Set();

        evalTypes.forEach(type => {
            const userIdsInColumn = existingAssignments
                .filter(a => a.evaluator.id === type.id)
                .map(a => a.evaluatorUser.id);
            // Burada `type.name` zaten işlenmiş olduğu için doğru başlık gelecektir.
            columns[type.id] = {id: type.id, title: type.name, userIds: userIdsInColumn};
            userIdsInColumn.forEach(id => assignedUserIds.add(id));
        });

        const availableUserIds = companyUsers
            .filter(u => !assignedUserIds.has(u.id) && u.id !== currentParticipant?.id)
            .map(u => u.id);

        columns['available'] = {id: 'available', title: 'Tüm Çalışanlar', userIds: availableUserIds};

        const columnOrder = ['available', ...evalTypes.map(t => t.id)];

        const selfAssignment = existingAssignments.find(
            a => a.evaluatorUser.id === currentParticipant?.id && a.evaluator.evaluatorType === 'SELF'
        );
        setIsSelfEvaluation(!!selfAssignment);

        setDndState({users, columns, columnOrder});
    }, []);

    const fetchInitialData = useCallback(async () => {
        if (!selectedPeriod) return;
        setLoading(true);
        try {
            const [usersRes, participantsRes, evaluatorTypesRes] = await Promise.all([
                getUsers({active: true}),
                getParticipantsByPeriod(selectedPeriod.id),
                getEvaluatorsByPeriodId(selectedPeriod.id)
            ]);

            const companyUsers = usersRes.data || [];
            const fetchedParticipants = participantsRes.data || [];

            // DEĞİŞİKLİK 2: Değerlendirici tiplerini işleyerek boş isimleri doldurun.
            const processedEvaluatorTypes = (evaluatorTypesRes.data || []).map(et => ({
                ...et,
                // Eğer `name` alanı boş veya null ise, çeviri nesnesinden varsayılan adı alınır.
                name: et.name || evaluatorTypeTranslations[et.evaluatorType] || 'Bilinmeyen Tip'
            }));

            // 'SELF' tipi, özel checkbox ile yönetildiği için D&D sütunlarından çıkarılır.
            const filteredEvaluatorTypes = processedEvaluatorTypes.filter(et => et.evaluatorType !== 'SELF');

            setAllCompanyUsers(companyUsers);
            setParticipants(fetchedParticipants);
            setEvaluatorTypes(filteredEvaluatorTypes); // İşlenmiş veriyi state'e kaydedin.

            const participantIds = new Set(fetchedParticipants.map(p => p.id));
            setAvailableUsers(companyUsers.filter(user => !participantIds.has(user.id)));

            if (fetchedParticipants.length > 0) {
                setSelectedParticipant(fetchedParticipants[0]);
            }

        } catch (error) {
            toast.error('Başlangıç verileri yüklenirken bir hata oluştu.');
            console.error(error);
        } finally {
            setLoading(false);
        }
    }, [selectedPeriod]);


    useEffect(() => {
        fetchInitialData();
    }, [fetchInitialData]);

    useEffect(() => {
        if (step === 2 && selectedParticipant) {
            setLoading(true);
            getParticipantAssignments(selectedPeriod.id, selectedParticipant.id)
                .then(res => {
                    initializeDndState(evaluatorTypes, allCompanyUsers, res.data, selectedParticipant);
                })
                .catch(err => {
                    toast.error('Atamalar yüklenemedi.');
                    console.error(err);
                    initializeDndState(evaluatorTypes, allCompanyUsers, [], selectedParticipant);
                })
                .finally(() => setLoading(false));
        }
    }, [step, selectedParticipant, selectedPeriod, evaluatorTypes, allCompanyUsers, initializeDndState]);


    const handleAddParticipant = async (user) => {
        try {
            await addParticipantToPeriod(selectedPeriod.id, user.id);
            setParticipants(prev => [...prev, user]);
            setAvailableUsers(prev => prev.filter(u => u.id !== user.id));
            toast.success(`${user.firstName} ${user.lastName} katılımcı olarak eklendi.`);
        } catch (error) {
            toast.error('Katılımcı eklenirken bir hata oluştu.');
        }
    };

    const handleRemoveParticipant = async (user) => {
        try {
            await deleteParticipantFromPeriod(selectedPeriod.id, user.id);
            setAvailableUsers(prev => [...prev, user]);
            setParticipants(prev => prev.filter(p => p.id !== user.id));
            toast.success(`${user.firstName} ${user.lastName} katılımcılardan çıkarıldı.`);
        } catch (error) {
            toast.error('Katılımcı çıkarılırken bir hata oluştu.');
        }
    };

    const onDragEnd = (result) => {
        const {destination, source, draggableId} = result;

        if (!destination) return;
        if (destination.droppableId === source.droppableId && destination.index === source.index) return;

        const startColumn = dndState.columns[source.droppableId];
        const finishColumn = dndState.columns[destination.droppableId];

        if (startColumn === finishColumn) {
            const newUserIds = Array.from(startColumn.userIds);
            newUserIds.splice(source.index, 1);
            newUserIds.splice(destination.index, 0, parseInt(draggableId));

            const newColumn = {...startColumn, userIds: newUserIds};
            setDndState(prev => ({
                ...prev,
                columns: {...prev.columns, [newColumn.id]: newColumn},
            }));
            return;
        }

        const startUserIds = Array.from(startColumn.userIds);
        startUserIds.splice(source.index, 1);
        const newStartColumn = {...startColumn, userIds: startUserIds};

        const finishUserIds = Array.from(finishColumn.userIds);
        finishUserIds.splice(destination.index, 0, parseInt(draggableId));
        const newFinishColumn = {...finishColumn, userIds: finishUserIds};

        setDndState(prev => ({
            ...prev,
            columns: {
                ...prev.columns,
                [newStartColumn.id]: newStartColumn,
                [newFinishColumn.id]: newFinishColumn,
            },
        }));
    };

    const handleSave = async () => {
        if (!selectedParticipant) return;

        try {
            // Self tipi için API'den gelen orijinal veriyi tekrar çekmek yerine işlenmiş veriden bulalım
            const allEvaluatorTypes = (await getEvaluatorsByPeriodId(selectedPeriod.id)).data;
            const selfEvaluatorType = allEvaluatorTypes.find(e => e.evaluatorType === 'SELF');

            let assignmentDetails = [];
            for (const columnId in dndState.columns) {
                if (columnId !== 'available') {
                    const column = dndState.columns[columnId];
                    column.userIds.forEach(userId => {
                        assignmentDetails.push({
                            evaluatorUserId: userId,
                            evaluatorId: parseInt(column.id)
                        });
                    });
                }
            }

            if (isSelfEvaluation) {
                if (selfEvaluatorType) {
                    if (!assignmentDetails.some(p => p.evaluatorUserId === selectedParticipant.id && p.evaluatorId === selfEvaluatorType.id)) {
                        assignmentDetails.push({
                            evaluatorUserId: selectedParticipant.id,
                            evaluatorId: selfEvaluatorType.id
                        });
                    }
                } else {
                    toast.error("'SELF' tipi bulunamadı, kendi değerlendirmesi kaydedilemedi.");
                }
            }

            const payload = {
                assignments: assignmentDetails
            };

            await saveParticipantAssignments(selectedPeriod.id, selectedParticipant.id, payload);
            toast.success('Atamalar başarıyla kaydedildi.');
        } catch (error) {
            toast.error('Atamalar kaydedilirken bir hata oluştu.');
            console.error('Save error:', error);
        }
    };

    const filteredAvailableUsers = useMemo(() => {
        return availableUsers.filter(user =>
            `${user.firstName} ${user.lastName}`.toLowerCase().includes(searchTerm.toLowerCase())
        );
    }, [availableUsers, searchTerm]);

    const filteredDndAvailableUsers = useMemo(() => {
        const availableColumn = dndState.columns['available'];
        if (!availableColumn) return [];

        return availableColumn.userIds.map(id => dndState.users[id]).filter(user =>
            user && `${user.firstName} ${user.lastName}`.toLowerCase().includes(assignmentSearchTerm.toLowerCase())
        );
    }, [dndState.columns, dndState.users, assignmentSearchTerm]);


    if (loading && !dndState.columnOrder.length) {
        return <div className="p-4">Yükleniyor...</div>;
    }

    if (!selectedPeriod) {
        return <div className="p-4">Lütfen önce bir değerlendirme dönemi seçin.</div>;
    }

    const renderStep1 = () => (
        <div className="grid grid-cols-2 gap-6">
            <div className="border rounded-lg p-4">
                <h2 className="text-lg font-semibold mb-4">Tüm Çalışanlar</h2>
                <Input
                    placeholder="Çalışan ara..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="mb-4"
                />
                <div className="overflow-auto h-96">
                    {filteredAvailableUsers.map(user => (
                        <div key={user.id}
                             className="flex items-center justify-between p-2 hover:bg-gray-100 rounded-md">
                            <span>{`${user.firstName} ${user.lastName} ${user.role ? ` - ${user.role}` : ''}`}</span>
                            <Button size="sm" onClick={() => handleAddParticipant(user)}>Ekle</Button>
                        </div>
                    ))}
                </div>
            </div>
            <div className="border rounded-lg p-4">
                <h2 className="text-lg font-semibold mb-4">Katılımcılar ({participants.length})</h2>
                <div className="overflow-auto h-96">
                    {participants.map(user => (
                        <div key={user.id}
                             className="flex items-center justify-between p-2 hover:bg-gray-100 rounded-md">
                            <span>{`${user.firstName} ${user.lastName} ${user.role ? ` - ${user.role}` : ''}`}</span>
                            <Button variant="destructive" size="sm"
                                    onClick={() => handleRemoveParticipant(user)}>Çıkar</Button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );

    const renderStep2 = () => (
        <DragDropContext onDragEnd={onDragEnd}>
            <div className="space-y-4">
                <div className="grid grid-cols-2 gap-6">
                    <div className="border rounded-lg p-4 space-y-4">
                        <div>
                            <label className="font-semibold">Değerlendirilen Çalışan</label>
                            <select
                                className="w-full p-2 border rounded mt-1"
                                value={selectedParticipant?.id || ''}
                                onChange={(e) => setSelectedParticipant(participants.find(p => p.id.toString() === e.target.value))}
                            >
                                {participants.map(p => (
                                    <option key={p.id} value={p.id}>
                                        {`${p.firstName} ${p.lastName} ${p.role ? ` - ${p.role}` : ''}`}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {dndState.columnOrder.slice(1).map(columnId => {
                            const column = dndState.columns[columnId];
                            const users = column.userIds.map(id => dndState.users[id]);
                            return (
                                <div key={column.id}>
                                    <h3 className="font-semibold">{column.title}</h3>
                                    <StrictModeDroppable droppableId={column.id.toString()}>
                                        {(provided, snapshot) => (
                                            <div
                                                ref={provided.innerRef}
                                                {...provided.droppableProps}
                                                className={`border rounded p-2 min-h-[50px] transition-colors ${snapshot.isDraggingOver ? 'bg-blue-100' : 'bg-gray-50'}`}>
                                                {users.map((user, index) => user && (
                                                    <Draggable key={user.id} draggableId={user.id.toString()}
                                                               index={index}>
                                                        {(provided) => (
                                                            <div
                                                                ref={provided.innerRef}
                                                                {...provided.draggableProps}
                                                                {...provided.dragHandleProps}
                                                                className="p-2 border rounded mb-2 bg-white cursor-grab shadow-sm"
                                                            >
                                                                {`${user.firstName} ${user.lastName} ${user.role ? ` - ${user.role}` : ''}`}
                                                            </div>
                                                        )}
                                                    </Draggable>
                                                ))}
                                                {provided.placeholder}
                                            </div>
                                        )}
                                    </StrictModeDroppable>
                                </div>
                            );
                        })}
                        <div>
                            <h3 className="font-semibold">Kendi</h3>
                            <div className="flex items-center space-x-2 mt-2 p-2 border rounded">
                                <input type="checkbox" id="self-evaluation" checked={isSelfEvaluation}
                                       onChange={(e) => setIsSelfEvaluation(e.target.checked)}/>
                                <label htmlFor="self-evaluation">
                                    {selectedParticipant ?
                                        `${selectedParticipant.firstName} ${selectedParticipant.lastName} ${selectedParticipant.role ?
                                            ` - ${selectedParticipant.role}` : ''
                                        }` : ''}
                                </label>
                            </div>
                        </div>
                    </div>

                    <div className="border rounded-lg p-4">
                        <h2 className="text-lg font-semibold mb-4">Tüm Çalışanlar</h2>
                        <Input
                            placeholder="Çalışan ara..."
                            value={assignmentSearchTerm}
                            onChange={(e) => setAssignmentSearchTerm(e.target.value)}
                            className="mb-4"
                        />
                        <StrictModeDroppable droppableId="available">
                            {(provided, snapshot) => (
                                <div
                                    ref={provided.innerRef}
                                    {...provided.droppableProps}
                                    className={`overflow-auto h-[450px] p-2 border rounded transition-colors ${snapshot.isDraggingOver ? 'bg-blue-100' : 'bg-gray-50'}`}>
                                    {filteredDndAvailableUsers.map((user, index) => user && (
                                        <Draggable key={user.id} draggableId={user.id.toString()} index={index}>
                                            {(provided) => (
                                                <div
                                                    ref={provided.innerRef}
                                                    {...provided.draggableProps}
                                                    {...provided.dragHandleProps}
                                                    className="p-2 border rounded mb-2 bg-white cursor-grab shadow-sm"
                                                >
                                                    {`${user.firstName} ${user.lastName} ${user.role ? ` - ${user.role}` : ''}`}
                                                </div>
                                            )}
                                        </Draggable>
                                    ))}
                                    {provided.placeholder}
                                </div>
                            )}
                        </StrictModeDroppable>
                    </div>
                </div>
            </div>
        </DragDropContext>
    );

    return (
        <div className="p-6 space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-semibold">Katılımcıları Belirle</h1>
                <div>
                    {step === 2 && <Button variant="outline" onClick={() => setStep(1)} className="mr-4">Geri</Button>}
                    {step === 1 && <Button onClick={() => setStep(2)} disabled={participants.length === 0}>Değerlendirici
                        Ata</Button>}
                    {step === 2 && <Button onClick={handleSave}>Kaydet</Button>}
                </div>
            </div>
            {step === 1 ? renderStep1() : renderStep2()}
        </div>
    );
};

export default ParticipantsPage;