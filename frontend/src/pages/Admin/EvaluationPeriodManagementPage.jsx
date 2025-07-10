import React, { useState, useEffect } from 'react';
import evaluationPeriodService from '../../services/evaluationPeriodService';
import evaluationTemplateService from '../../services/evaluationTemplateService';
import { EvaluationPeriodRequest } from '../../models/request/EvaluationPeriodRequest';
import Modal from '../../components/Modal';

const EvaluationPeriodManagementPage = () => {
        const [periods, setPeriods] = useState([]);
    const [templates, setTemplates] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newPeriod, setNewPeriod] = useState({
        name: '',
        description: '',
        startDate: '',
        endDate: '',
        templateIds: [],
        isSelfEvaluationIncluded: false,
    });

        useEffect(() => {
        fetchPeriods();
        fetchTemplates();
    }, []);

        const fetchTemplates = async () => {
        try {
            const response = await evaluationTemplateService.getAllTemplates();
            setTemplates(response.data);
        } catch (error) {
            console.error('Error fetching evaluation templates:', error);
        }
    };

    const fetchPeriods = async () => {
        try {
            const response = await evaluationPeriodService.getAllPeriods();
            setPeriods(response.data);
        } catch (error) {
            console.error('Error fetching evaluation periods:', error);
        }
    };

    const handleCreatePeriod = async (e) => {
        e.preventDefault();
        try {
            const periodRequest = new EvaluationPeriodRequest(
                newPeriod.name,
                newPeriod.startDate,
                newPeriod.endDate,
                newPeriod.templateIds,
                newPeriod.isSelfEvaluationIncluded
            );
            await evaluationPeriodService.createEvaluationPeriod(periodRequest);
            fetchPeriods();
            setIsModalOpen(false);
                        setNewPeriod({ name: '', description: '', startDate: '', endDate: '', templateIds: [], isSelfEvaluationIncluded: false });
        } catch (error) {
            console.error('Error creating evaluation period:', error);
        }
    };

    const handleInputChange = (e) => {
                const { name, value, type, checked } = e.target;
        if (type === 'checkbox') {
            if (name === 'isSelfEvaluationIncluded') {
                setNewPeriod(prev => ({ ...prev, isSelfEvaluationIncluded: checked }));
            } else {
                const templateId = parseInt(name);
                setNewPeriod(prev => {
                    const newTemplateIds = checked
                        ? [...prev.templateIds, templateId]
                        : prev.templateIds.filter(id => id !== templateId);
                    return { ...prev, templateIds: newTemplateIds };
                });
            }
        } else {
                    setNewPeriod({ ...newPeriod, [name]: value });
        }
    };

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Evaluation Period Management</h1>
                <button onClick={() => setIsModalOpen(true)} className="px-4 py-2 text-white bg-blue-600 rounded hover:bg-blue-700">
                    Create Period
                </button>
            </div>

            <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Create Evaluation Period">
                <form onSubmit={handleCreatePeriod}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Name</label>
                        <input type="text" name="name" value={newPeriod.name} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Description</label>
                        <input type="text" name="description" value={newPeriod.description} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Start Date</label>
                        <input type="date" name="startDate" value={newPeriod.startDate} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>
                                        <div className="mb-4">
                        <label className="block text-gray-700">End Date</label>
                        <input type="date" name="endDate" value={newPeriod.endDate} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>

                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2 font-semibold">Evaluation Templates</label>
                        <div className="max-h-48 overflow-y-auto border rounded-md p-3 bg-gray-50">
                            {templates.map(template => (
                                <div key={template.id} className="flex items-center mb-2">
                                    <input
                                        type="checkbox"
                                        id={`template-${template.id}`}
                                        name={template.id}
                                        checked={newPeriod.templateIds.includes(template.id)}
                                        onChange={handleInputChange}
                                        className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                                    />
                                    <label htmlFor={`template-${template.id}`} className="ml-3 block text-sm text-gray-900">
                                        {template.name}
                                    </label>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="mb-4">
                        <div className="flex items-center">
                            <input
                                type="checkbox"
                                id="isSelfEvaluationIncluded"
                                name="isSelfEvaluationIncluded"
                                checked={newPeriod.isSelfEvaluationIncluded}
                                onChange={handleInputChange}
                                className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                            />
                            <label htmlFor="isSelfEvaluationIncluded" className="ml-3 block text-sm text-gray-900">
                                Include Self-Evaluation
                            </label>
                        </div>
                    </div>
                    <div className="flex justify-end">
                        <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 mr-2 text-gray-700 bg-gray-200 rounded hover:bg-gray-300">
                            Cancel
                        </button>
                        <button type="submit" className="px-4 py-2 text-white bg-blue-600 rounded hover:bg-blue-700">
                            Create
                        </button>
                    </div>
                </form>
            </Modal>

            <div className="bg-white shadow-md rounded-lg">
                <table className="min-w-full leading-normal">
                    <thead>
                        <tr>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Description</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Start Date</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">End Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {periods.map((period) => (
                            <tr key={period.id}>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{period.name}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{period.description}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{new Date(period.startDate).toLocaleDateString()}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{new Date(period.endDate).toLocaleDateString()}</p>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default EvaluationPeriodManagementPage;
