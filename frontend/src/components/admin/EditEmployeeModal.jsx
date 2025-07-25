import React, { useState, useEffect } from 'react';
import UserService from '../../services/userService';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Checkbox } from '../ui/checkbox';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '../ui/dialog';

const EditEmployeeModal = ({ isOpen, onClose, onSuccess, employee }) => {
    const [formData, setFormData] = useState({});
    const [error, setError] = useState('');

    useEffect(() => {
        if (employee) {
            setFormData({
                firstName: employee.firstName || '',
                lastName: employee.lastName || '',
                email: employee.email || '',
                isAdmin: employee.role === 'ADMIN',
                isActive: employee.isActive,
            });
        } else {
            setFormData({
                firstName: '',
                lastName: '',
                email: '',
                isAdmin: false,
                isActive: true,
            });
        }
    }, [employee]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleCheckedChange = (name, checked) => {
        setFormData(prev => ({ ...prev, [name]: checked }));
    }

    const handleSubmit = (e) => {
        e.preventDefault();
        setError('');

        if (!formData.firstName || !formData.lastName || !formData.email) {
            setError('Lütfen tüm zorunlu alanları doldurun.');
            return;
        }

        UserService.updateUser(employee.id, formData)
            .then(() => {
                onSuccess();
            })
            .catch(err => {
                console.error('Error updating user:', err);
                setError('Çalışan güncellenirken bir hata oluştu.');
            });
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>Çalışanı Düzenle</DialogTitle>
                    <DialogDescription>
                        Çalışan bilgilerini güncelleyin.
                    </DialogDescription>
                </DialogHeader>
                <form onSubmit={handleSubmit}>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="firstName" className="text-right">İsim</Label>
                            <Input id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="lastName" className="text-right">Soyisim</Label>
                            <Input id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="email" className="text-right">Email</Label>
                            <Input id="email" name="email" type="email" value={formData.email} onChange={handleChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="role" className="text-right">Pozisyon</Label>
                            <Input id="role" name="role" value={formData.role} onChange={handleChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="isAdmin" className="text-right">Admin mi?</Label>
                            <Checkbox id="isAdmin" checked={formData.isAdmin} onCheckedChange={(checked) => handleCheckedChange('isAdmin', checked)} />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="isActive" className="text-right">Aktif mi?</Label>
                            <Checkbox id="isActive" checked={formData.isActive} onCheckedChange={(checked) => handleCheckedChange('isActive', checked)} />
                        </div>
                    </div>
                    {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
                    <DialogFooter>
                        <Button type="button" variant="secondary" onClick={onClose}>İptal</Button>
                        <Button type="submit">Kaydet</Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default EditEmployeeModal;
