import React, { useState, useEffect, useCallback } from 'react';
import UserService from '../../services/userService';
import { Button } from '../../components/ui/button';
import { Checkbox } from '../../components/ui/checkbox';
import { Input } from '../../components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Plus, Upload, Download } from 'lucide-react';
import AddEmployeeModal from '../../components/admin/AddEmployeeModal';
import EditEmployeeModal from '../../components/admin/EditEmployeeModal';

const EmployeesPage = () => {
    const [employees, setEmployees] = useState([]);
    const [selectedEmployees, setSelectedEmployees] = useState([]);
    const [filters, setFilters] = useState({ name: '', status: 'all' });
        const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [selectedEmployeeForEdit, setSelectedEmployeeForEdit] = useState(null);

    const fetchEmployees = useCallback(() => {
        const params = {};
        if (filters.name) {
            params.name = filters.name;
        }
        if (filters.status !== 'all') {
            params.active = filters.status === 'active';
        }

        UserService.getUsers(params)
            .then(response => {
                setEmployees(response.data || []);
            })
            .catch(error => console.error('Error fetching employees:', error));
    }, [filters]);

    useEffect(() => {
        fetchEmployees();
    }, [fetchEmployees]);

    const handleSelectAll = (checked) => {
        if (checked) {
            setSelectedEmployees(employees.map(e => e.id));
        } else {
            setSelectedEmployees([]);
        }
    };

    const handleSelectOne = (id) => {
        setSelectedEmployees(prev =>
            prev.includes(id) ? prev.filter(eId => eId !== id) : [...prev, id]
        );
    };

    const handleFilterChange = (key, value) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

        const updateSelectedEmployeesStatus = (isActive) => {
        const promises = selectedEmployees.map(id => {
            const employee = employees.find(e => e.id === id);
            if (employee) {
                // The API expects the full user object for an update.
                // We provide the existing data and just change the active status.
                const userRequest = {
                    ...employee,
                    isActive: isActive
                };
                return UserService.updateUser(id, userRequest);
            }
            return Promise.resolve();
        });

        Promise.all(promises)
            .then(() => {
                fetchEmployees();
                setSelectedEmployees([]);
            })
            .catch(error => console.error(`Error updating status to ${isActive}:`, error));
    };

    return (
        <div className="container mx-auto p-4">
            <div className="flex justify-between items-center mb-4">
                <h1 className="text-2xl font-bold">Çalışanlar</h1>
                <div className="flex items-center gap-2">
                    <Button variant="outline" disabled><Download className="mr-2 h-4 w-4" /> İçe Aktar</Button>
                    <Button variant="outline" disabled><Upload className="mr-2 h-4 w-4" /> Dışa Aktar</Button>
                                        <Button onClick={() => setIsAddModalOpen(true)}><Plus className="mr-2 h-4 w-4" /> Çalışan Ekle</Button>
                </div>
            </div>

            <div className="bg-white p-4 rounded-lg shadow">
                <div className="flex flex-col md:flex-row gap-4 mb-4">
                    <Input
                        placeholder="Tüm Çalışanlar"
                        value={filters.name}
                        onChange={(e) => handleFilterChange('name', e.target.value)}
                        className="max-w-xs"
                    />
                    {/* Position filter placeholder - functionality not implemented */}
                    <Select disabled>
                        <SelectTrigger className="w-[180px]">
                            <SelectValue placeholder="Tüm Pozisyonlar" />
                        </SelectTrigger>
                    </Select>
                    <Select onValueChange={(value) => handleFilterChange('status', value)} defaultValue="all">
                        <SelectTrigger className="w-[180px]">
                            <SelectValue placeholder="Tüm Durumlar" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">Tüm Durumlar</SelectItem>
                            <SelectItem value="active">Aktif</SelectItem>
                            <SelectItem value="inactive">Pasif</SelectItem>
                        </SelectContent>
                    </Select>
                </div>

                <div className="flex items-center gap-2 mb-4">
                    <Button size="sm" onClick={() => updateSelectedEmployeesStatus(false)} disabled={selectedEmployees.length === 0}>Seçili Çalışanı Pasif Et</Button>
                    <Button size="sm" onClick={() => updateSelectedEmployeesStatus(true)} disabled={selectedEmployees.length === 0}>Seçili Çalışanı Aktif Et</Button>
                </div>

                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[50px]">
                                <Checkbox
                                    checked={selectedEmployees.length > 0 && selectedEmployees.length === employees.length}
                                    onCheckedChange={handleSelectAll}
                                />
                            </TableHead>
                            <TableHead>İsim</TableHead>
                            <TableHead>Pozisyon</TableHead>
                            <TableHead>Durum</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {employees.map((employee) => (
                                                        <TableRow 
                                key={employee.id}
                                onClick={() => {
                                    setSelectedEmployeeForEdit(employee);
                                    setIsEditModalOpen(true);
                                }}
                                className="cursor-pointer"
                            >
                                                                <TableCell onClick={(e) => e.stopPropagation()}>
                                    <Checkbox
                                        checked={selectedEmployees.includes(employee.id)}
                                        onCheckedChange={() => handleSelectOne(employee.id)}
                                    />
                                </TableCell>
                                <TableCell>{`${employee.firstName} ${employee.lastName}`}</TableCell>
                                <TableCell>-
                                    {/* Position data not available */}
                                </TableCell>
                                <TableCell>{employee.isActive ? 'Aktif' : 'Pasif'}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>

                        <AddEmployeeModal
                isOpen={isAddModalOpen}
                onClose={() => setIsAddModalOpen(false)}
                onSuccess={() => {
                    setIsAddModalOpen(false);
                    fetchEmployees();
                }}
            />

            {selectedEmployeeForEdit && (
                <EditEmployeeModal
                    isOpen={isEditModalOpen}
                    onClose={() => {
                        setIsEditModalOpen(false);
                        setSelectedEmployeeForEdit(null);
                    }}
                    onSuccess={() => {
                        setIsEditModalOpen(false);
                        setSelectedEmployeeForEdit(null);
                        fetchEmployees();
                    }}
                    employee={selectedEmployeeForEdit}
                />
            )}
        </div>
    );
};

export default EmployeesPage;
