import React, { useState, useEffect } from 'react';
import RoleService from '../../services/roleService';
import { RoleRequest } from '../../models/request/RoleRequest';
import Modal from '../../components/Modal';

const RoleManagementPage = () => {
    const [roles, setRoles] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newRoleName, setNewRoleName] = useState('');

    useEffect(() => {
        fetchRoles();
    }, []);

    const fetchRoles = async () => {
        try {
            const response = await RoleService.getAllRoles();
            setRoles(response.data);
        } catch (error) {
            console.error('Error fetching roles:', error);
        }
    };

    const handleCreateRole = async (e) => {
        e.preventDefault();
        try {
            const roleRequest = new RoleRequest(newRoleName);
            await RoleService.createRole(roleRequest);
            fetchRoles();
            setIsModalOpen(false);
            setNewRoleName('');
        } catch (error) {
            console.error('Error creating role:', error);
        }
    };

    const handleDeleteRole = async (roleId) => {
        if (window.confirm('Are you sure you want to delete this role?')) {
            try {
                await RoleService.deleteRole(roleId);
                fetchRoles();
            } catch (error) {
                console.error('Error deleting role:', error);
            }
        }
    };

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Role Management</h1>
                <button onClick={() => setIsModalOpen(true)} className="px-4 py-2 text-white bg-blue-600 rounded hover:bg-blue-700">
                    Create Role
                </button>
            </div>

            <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Create Role">
                <form onSubmit={handleCreateRole}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Role Name</label>
                        <input 
                            type="text" 
                            value={newRoleName}
                            onChange={(e) => setNewRoleName(e.target.value)}
                            className="w-full p-2 border rounded" 
                            required 
                        />
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
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Role Name</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {roles.map((role) => (
                            <tr key={role.id}>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{role.name}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <button onClick={() => handleDeleteRole(role.id)} className="text-red-600 hover:text-red-800">Delete</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default RoleManagementPage;
