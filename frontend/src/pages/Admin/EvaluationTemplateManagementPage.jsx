import React, { useState, useEffect } from 'react';
import evaluationTemplateService from '../../services/evaluationTemplateService';
import roleService from '../../services/roleService';
import { EvaluationTemplateRequest } from '../../models/request/EvaluationTemplateRequest';
import { QuestionRequest } from '../../models/request/QuestionRequest';
import Modal from '../../components/Modal';
import { QuestionType } from '../../models/enums/QuestionType';

const EvaluationTemplateManagementPage = () => {
    const [templates, setTemplates] = useState([]);
    const [roles, setRoles] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('create'); // 'create' or 'edit'
    const [currentTemplate, setCurrentTemplate] = useState(null);
        const [newQuestion, setNewQuestion] = useState('');
    const [newQuestionType, setNewQuestionType] = useState('TEXT');

    useEffect(() => {
        fetchTemplates();
        fetchRoles();
    }, []);

    const fetchTemplates = async () => {
        try {
            const response = await evaluationTemplateService.getAllTemplates();
            setTemplates(response.data);
        } catch (error) {
            console.error('Error fetching evaluation templates:', error);
        }
    };

    const fetchRoles = async () => {
        try {
            const response = await roleService.getAllRoles();
            setRoles(response.data);
        } catch (error) {
            console.error('Error fetching roles:', error);
        }
    };

    const handleFormSubmit = async (e) => {
        e.preventDefault();
        const { id, name, description, targetRoleId } = currentTemplate;
        const request = new EvaluationTemplateRequest(name, description, parseInt(targetRoleId));

        try {
            if (modalMode === 'create') {
                await evaluationTemplateService.createTemplate(request);
            } else {
                await evaluationTemplateService.updateTemplate(id, request);
            }
            fetchTemplates();
            closeModal();
        } catch (error) {
            console.error('Error saving template:', error);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCurrentTemplate({ ...currentTemplate, [name]: value });
    };

    const openCreateModal = () => {
        setModalMode('create');
        setCurrentTemplate({ name: '', description: '', targetRoleId: '' });
        setIsModalOpen(true);
    };

    const openEditModal = (template) => {
        setModalMode('edit');
        setCurrentTemplate(template);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setCurrentTemplate(null);
        setNewQuestion('');
    };

    const refreshTemplateInModal = async () => {
        try {
            // In a real app, you might fetch a single template by ID
            // For simplicity, we'll refetch all and find the one we need.
            const response = await evaluationTemplateService.getAllTemplates();
            const updatedTemplate = response.data.find(t => t.id === currentTemplate.id);
            if (updatedTemplate) {
                setCurrentTemplate(updatedTemplate);
            }
            // Also update the main list
            setTemplates(response.data);
        } catch (error) {
            console.error('Error refreshing template data:', error);
        }
    };

        const handleAddQuestion = async () => {
        if (!newQuestion.trim()) return;
        try {
            const request = new QuestionRequest(newQuestion, newQuestionType);
            await evaluationTemplateService.addQuestionToTemplate(currentTemplate.id, request);
            setNewQuestion('');
            setNewQuestionType('TEXT');
            refreshTemplateInModal();
        } catch (error) {
            console.error('Error adding question:', error);
        }
    };

    const handleRemoveQuestion = async (questionId) => {
        try {
            await evaluationTemplateService.removeQuestionFromTemplate(currentTemplate.id, questionId);
            refreshTemplateInModal();
        } catch (error) {
            console.error('Error removing question:', error);
        }
    };

    const handleDeleteTemplate = async (templateId) => {
        if (window.confirm('Are you sure you want to delete this template?')) {
            try {
                await evaluationTemplateService.deleteTemplate(templateId);
                fetchTemplates();
            } catch (error) {
                console.error('Error deleting template:', error);
            }
        }
    };

    const handleVisibilityChange = async (e) => {
        const selectedRoleIds = Array.from(e.target.selectedOptions, option => parseInt(option.value));
        const request = selectedRoleIds.map(id => ({ evaluatorRoleId: id }));
        try {
            await evaluationTemplateService.setTemplateVisibility(currentTemplate.id, request);
            refreshTemplateInModal();
        } catch (error) {
            console.error('Error updating visibility:', error);
        }
    };

    const renderModalContent = () => {
        if (!currentTemplate) return null;

        const title = modalMode === 'create' ? 'Create Evaluation Template' : 'Edit Evaluation Template';

        return (
            <Modal isOpen={isModalOpen} onClose={closeModal} title={title}>
                {/* Template Details Form */}
                <form onSubmit={handleFormSubmit} className="mb-6">
                    <h3 className="text-lg font-semibold mb-2">Template Details</h3>
                    <div className="mb-4">
                        <label className="block text-gray-700">Name</label>
                        <input type="text" name="name" value={currentTemplate.name} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Description</label>
                        <textarea name="description" value={currentTemplate.description} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Target Role</label>
                        <select name="targetRoleId" value={currentTemplate.targetRoleId} onChange={handleInputChange} className="w-full p-2 border rounded" required>
                            <option value="">Select a role</option>
                            {roles.map(role => <option key={role.id} value={role.id}>{role.name}</option>)}
                        </select>
                    </div>
                    <div className="flex justify-end">
                        <button type="submit" className="px-4 py-2 text-white bg-blue-600 rounded hover:bg-blue-700">
                            {modalMode === 'create' ? 'Create' : 'Save Changes'}
                        </button>
                    </div>
                </form>

                {modalMode === 'edit' && (
                    <>
                        {/* Question Management */}
                        <div className="mb-6">
                            <h3 className="text-lg font-semibold mb-2">Questions</h3>
                            <ul className="mb-2 list-disc list-inside">
                                {currentTemplate.questions.map(q => (
                                    <li key={q.id} className="flex justify-between items-center">
                                        <span>{q.question}</span>
                                        <button onClick={() => handleRemoveQuestion(q.id)} className="text-red-500 hover:text-red-700">Remove</button>
                                    </li>
                                ))}
                            </ul>
                                                                                    <div className="relative flex w-full">
                                <input type="text" value={newQuestion} onChange={(e) => setNewQuestion(e.target.value)} placeholder="Add new question" className="flex-1 p-2 border border-r-0 rounded-l-md focus:ring-indigo-500 focus:border-indigo-500 block w-full sm:text-sm border-gray-300" />
                                <select value={newQuestionType} onChange={(e) => setNewQuestionType(e.target.value)} className="p-2 border-t border-b bg-white border-gray-300 text-sm">
                                    {Object.values(QuestionType).map(type => (
                                        <option key={type} value={type}>{type.replace(/_/g, ' ')}</option>
                                    ))}
                                </select>
                                <button onClick={handleAddQuestion} className="px-4 py-2 text-white bg-green-600 rounded-r-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 text-sm">
                                    Add
                                </button>
                            </div>
                        </div>

                        {/* Visibility Management */}
                        <div>
                            <h3 className="text-lg font-semibold mb-2">Evaluator Roles</h3>
                            <p className="text-sm text-gray-600 mb-2">Select roles that can use this template to evaluate others. (Cmd/Ctrl + Click for multiple)</p>
                            <select multiple value={currentTemplate.evaluatorRoles.map(r => r.id)} onChange={handleVisibilityChange} className="w-full p-2 border rounded h-32">
                                {roles.map(role => <option key={role.id} value={role.id}>{role.name}</option>)}
                            </select>
                        </div>
                    </>
                )}
            </Modal>
        );
    };

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Evaluation Template Management</h1>
                <button onClick={openCreateModal} className="px-4 py-2 text-white bg-blue-600 rounded hover:bg-blue-700">
                    Create Template
                </button>
            </div>

            {renderModalContent()}

            <div className="bg-white shadow-md rounded-lg">
                <table className="min-w-full leading-normal">
                    <thead>
                        <tr>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Description</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Target Role</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {templates.map((template) => (
                            <tr key={template.id}>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{template.name}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{template.description}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{roles.find(r => r.id === template.targetRoleId)?.name || 'N/A'}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <button onClick={() => openEditModal(template)} className="text-blue-600 hover:text-blue-800 mr-4">View/Edit</button>
                                    <button onClick={() => handleDeleteTemplate(template.id)} className="text-red-600 hover:text-red-800">Delete</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default EvaluationTemplateManagementPage;
