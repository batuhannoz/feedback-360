import React, { useState, useEffect } from 'react';
import { Form, Button, Table, Alert, Card } from 'react-bootstrap';
import periodEvaluatorService from '../../services/periodEvaluatorService';
import { useSelector } from 'react-redux';

const ResourcesPage = () => {
    const [resources, setResources] = useState([]);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const selectedPeriod = useSelector(state => state.period.selectedPeriod);

    const evaluatorTypes = [
        { type: 'SELF', defaultName: 'Kendim' },
        { type: 'MANAGER', defaultName: 'Yöneticim' },
        { type: 'PEER', defaultName: 'Ekip Arkadaşım' },
        { type: 'SUBORDINATE', defaultName: 'Astım' },
        { type: 'CUSTOMER', defaultName: 'Müşterim' },
    ];

    useEffect(() => {
        if (selectedPeriod) {
            periodEvaluatorService.getPeriodEvaluators(selectedPeriod.id)
                .then(response => {
                    const existingEvaluators = response.data;
                    const initialResources = evaluatorTypes.map(typeInfo => {
                        const existing = existingEvaluators.find(e => e.type === typeInfo.type);
                        return {
                            id: existing ? existing.id : null,
                            type: typeInfo.type,
                            name: existing ? existing.name : '',
                            defaultName: typeInfo.defaultName
                        };
                    });
                    setResources(initialResources);
                })
                .catch(error => {
                    console.error('Error fetching resources:', error);
                    setError('Kaynaklar alınırken bir hata oluştu.');
                });
        }
    }, [selectedPeriod]);

    const handleNameChange = (type, newName) => {
        setResources(resources.map(r => r.type === type ? { ...r, name: newName } : r));
    };

    const handleSave = () => {
        if (!selectedPeriod) {
            setError('Lütfen önce bir dönem seçin.');
            return;
        }
        setError('');
        setSuccess('');

        const payload = resources.map(r => ({
            id: r.id,
            name: r.name.trim() || r.defaultName,
            type: r.type,
            period: { id: selectedPeriod.id }
        }));

        periodEvaluatorService.saveOrUpdatePeriodEvaluators(payload)
            .then(response => {
                const updatedResources = evaluatorTypes.map(typeInfo => {
                    const updated = response.data.find(e => e.type === typeInfo.type);
                    return {
                        id: updated ? updated.id : null,
                        type: typeInfo.type,
                        name: updated ? updated.name : '',
                        defaultName: typeInfo.defaultName
                    };
                });
                setResources(updatedResources);
                setSuccess('Kaynaklar başarıyla güncellendi.');
            })
            .catch(error => {
                console.error('Error saving resources:', error);
                setError('Kaynaklar güncellenirken bir hata oluştu.');
            });
    };

    return (
        <div className="container mt-4">
             <Card>
                <Card.Header as="h5">Kaynak İsimlerini Özelleştir</Card.Header>
                <Card.Body>
                    <Card.Title>Değerlendirme Kaynakları</Card.Title>
                    <Card.Text>
                        Bu sayfada, değerlendirme sürecinde kullanılacak kaynakların (Örn: Yönetici, Ekip Arkadaşı) isimlerini projenize özel olarak belirleyebilirsiniz. Boş bırakılan alanlar için varsayılan isimler kullanılacaktır.
                    </Card.Text>
                    {error && <Alert variant="danger">{error}</Alert>}
                    {success && <Alert variant="success">{success}</Alert>}
                    {selectedPeriod ? (
                        <Form>
                            <Table striped bordered hover className='mt-3'>
                                <thead>
                                    <tr>
                                        <th>Kaynak Tipi</th>
                                        <th>Özel İsim (Boşsa varsayılan kullanılır)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {resources.map(resource => (
                                        <tr key={resource.type}>
                                            <td>{resource.defaultName}</td>
                                            <td>
                                                <Form.Control
                                                    type="text"
                                                    placeholder={`Varsayılan: ${resource.defaultName}`}
                                                    value={resource.name}
                                                    onChange={(e) => handleNameChange(resource.type, e.target.value)}
                                                />
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                            <Button variant="primary" onClick={handleSave} className="mt-3">
                                Kaydet
                            </Button>
                        </Form>
                    ) : (
                        <Alert variant="warning">Kaynakları yönetmek için lütfen bir değerlendirme dönemi seçin.</Alert>
                    )}
                </Card.Body>
            </Card>
        </div>
    );
};

export default ResourcesPage;
