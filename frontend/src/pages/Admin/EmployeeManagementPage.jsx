import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import EmployeeService from '../../services/employeeService';
import RoleService from '../../services/roleService';
import { EmployeeRequest } from '../../models/request/EmployeeRequest';
import Modal from '../../components/Modal';

const EmployeeManagementPage = () => {
    const [employees, setEmployees] = useState([]);
    const [roles, setRoles] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isEditMode, setIsEditMode] = useState(false);
    const [selectedEmployee, setSelectedEmployee] = useState(null);
    const [formData, setFormData] = useState({ firstName: '', lastName: '', email: '', isAdmin: false });
    const [selectedRole, setSelectedRole] = useState('');

    useEffect(() => {
        fetchEmployees();
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

    const fetchEmployees = async () => {
        try {
            const response = await EmployeeService.listEmployees();
            setEmployees(response.data);
        } catch (error) {
            console.error('Error fetching employees:', error);
        }
    };

    const handleFormSubmit = async (e) => {
        e.preventDefault();
        if (isEditMode) {
            await handleUpdateEmployee();
        } else {
            await handleCreateEmployee();
        }
    };

    const handleCreateEmployee = async () => {
        try {
            const employeeRequest = new EmployeeRequest(formData.firstName, formData.lastName, formData.email, formData.isAdmin);
            await EmployeeService.createEmployee(employeeRequest);
            fetchEmployees();
            closeModal();
        } catch (error) {
            console.error('Error creating employee:', error);
        }
    };

    const handleUpdateEmployee = async () => {
        try {
            const employeeRequest = new EmployeeRequest(formData.firstName, formData.lastName, formData.email, formData.isAdmin);
            await EmployeeService.updateEmployee(selectedEmployee.id, employeeRequest);
            fetchEmployees();
            closeModal();
        } catch (error) {
            console.error('Error updating employee:', error);
        }
    };

    const handleAddRole = async () => {
        if (selectedEmployee && selectedRole) {
            try {
                await EmployeeService.assignRoleToEmployee(selectedEmployee.id, selectedRole);
                const updatedEmployee = await EmployeeService.getEmployeeDetails(selectedEmployee.id);
                setSelectedEmployee(updatedEmployee.data);
                fetchEmployees();
                setSelectedRole('');
            } catch (error) {
                console.error('Error assigning role:', error);
            }
        }
    };

    const handleRemoveRole = async (roleId) => {
        if (selectedEmployee) {
            try {
                await EmployeeService.removeRoleFromEmployee(selectedEmployee.id, roleId);
                const updatedEmployee = await EmployeeService.getEmployeeDetails(selectedEmployee.id);
                setSelectedEmployee(updatedEmployee.data);
                fetchEmployees();
            } catch (error) {
                console.error('Error removing role:', error);
            }
        }
    };

    const handleDeleteEmployee = async (employeeId) => {
        if (window.confirm('Are you sure you want to delete this employee?')) {
            try {
                await EmployeeService.deleteEmployee(employeeId);
                fetchEmployees();
            } catch (error) {
                console.error('Error deleting employee:', error);
            }
        }
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData({ ...formData, [name]: type === 'checkbox' ? checked : value });
    };

    const openModal = (employee = null) => {
        if (employee) {
            setIsEditMode(true);
            setSelectedEmployee(employee);
            const hasAdminRole = employee.roles.some(role => role.name === 'ADMIN');
            setFormData({ firstName: employee.firstName, lastName: employee.lastName, email: employee.email, isAdmin: hasAdminRole });
        } else {
            setIsEditMode(false);
            setSelectedEmployee(null);
            setFormData({ firstName: '', lastName: '', email: '', isAdmin: false });
        }
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setSelectedEmployee(null);
        setSelectedRole('');
    };

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Employee Management</h1>
                <button onClick={() => openModal()} className="px-4 py-2 text-white bg-blue-600 rounded hover:bg-blue-700">
                    Create Employee
                </button>
            </div>

            <Modal isOpen={isModalOpen} onClose={closeModal} title={isEditMode ? `Edit ${selectedEmployee?.firstName}` : 'Create Employee'}>
                <form onSubmit={handleFormSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700">First Name</label>
                        <input type="text" name="firstName" value={formData.firstName} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Last Name</label>
                        <input type="text" name="lastName" value={formData.lastName} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Email</label>
                        <input type="email" name="email" value={formData.email} onChange={handleInputChange} className="w-full p-2 border rounded" required />
                    </div>
                    <div className="mb-4">
                        <label className="flex items-center">
                            <input type="checkbox" name="isAdmin" checked={formData.isAdmin} onChange={handleInputChange} className="mr-2" />
                            Is Admin?
                        </label>
                    </div>

                    {isEditMode && selectedEmployee && (
                        <div className="mb-4 p-4 border-t">
                            <label className="block text-gray-700 font-semibold mb-2">Role Management</label>
                            <div className="flex items-center mb-2">
                                <select onChange={(e) => setSelectedRole(e.target.value)} value={selectedRole} className="w-full p-2 border rounded">
                                    <option value="">Select a role to add</option>
                                    {roles
                                        .filter(role => !selectedEmployee.roles.some(r => r.id === role.id))
                                        .map(role => (
                                            <option key={role.id} value={role.id}>{role.name}</option>
                                        ))}
                                </select>
                                <button type="button" onClick={handleAddRole} className="ml-2 px-4 py-2 text-white bg-green-500 rounded hover:bg-green-600 disabled:bg-gray-400" disabled={!selectedRole}>Add</button>
                            </div>
                            <div>
                                <h4 className="text-sm text-gray-600 mt-3">Current Roles:</h4>
                                {selectedEmployee.roles.length > 0 ? (
                                    selectedEmployee.roles.map(role => (
                                        <div key={role.id} className="flex items-center justify-between bg-gray-100 p-2 rounded mt-1">
                                            <span>{role.name}</span>
                                            <button type="button" onClick={() => handleRemoveRole(role.id)} className="text-red-500 hover:text-red-700 text-xs font-bold">X</button>
                                        </div>
                                    ))
                                ) : (
                                    <p className="text-sm text-gray-500">No roles assigned.</p>
                                )}
                            </div>
                        </div>
                    )}

                    <div className="flex justify-end pt-4 border-t">
                        <button type="button" onClick={closeModal} className="px-4 py-2 mr-2 text-gray-700 bg-gray-200 rounded hover:bg-gray-300">
                            Cancel
                        </button>
                        <button type="submit" className="px-4 py-2 text-white bg-blue-600 rounded hover:bg-blue-700">
                            {isEditMode ? 'Update' : 'Create'}
                        </button>
                    </div>
                </form>
            </Modal>

            <div className="bg-white shadow-md rounded-lg">
                <table className="min-w-full leading-normal">
                    <thead>
                        <tr>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Email</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Roles</th>
                            <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {employees.map((employee) => (
                            <tr key={employee.id}>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <Link to={`/admin/employee/${employee.id}/evaluations`} className="text-gray-900 whitespace-no-wrap hover:underline">
                                        {employee.firstName} {employee.lastName}
                                    </Link>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{employee.email}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <p className="text-gray-900 whitespace-no-wrap">{employee.roles.map(r => r.name).join(', ')}</p>
                                </td>
                                <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                    <button onClick={() => openModal(employee)} className="text-indigo-600 hover:text-indigo-900 mr-4">Edit</button>
                                    <button onClick={() => handleDeleteEmployee(employee.id)} className="text-red-600 hover:text-red-900">Delete</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default EmployeeManagementPage;
