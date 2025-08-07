import React, { useState, useEffect, useCallback } from 'react';
import { toast } from 'react-toastify';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Textarea } from '../../components/ui/textarea';
import { Upload, Save } from 'lucide-react';
import CompanySettingsService from '../../services/companyService';

const CompanySettingsPage = () => {
    const [settings, setSettings] = useState({
        name: '',
        email: '',
        phoneNumber: '',
        address: '',
        website: '',
        emailFooter: ''
    });
    const [logoUrl, setLogoUrl] = useState('');
    const [selectedLogo, setSelectedLogo] = useState(null);
    const [previewLogo, setPreviewLogo] = useState('');
    const [loading, setLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        let currentLogoUrl = '';

        const loadData = async () => {
            try {
                setLoading(true);
                const settingsResponse = await CompanySettingsService.getCompanySettings();
                setSettings(settingsResponse.data);

                const logoResponse = await CompanySettingsService.getLogoUrl();
                currentLogoUrl = URL.createObjectURL(logoResponse.data);
                setLogoUrl(currentLogoUrl);

            } catch (error) {
                console.error("Veriler getirilirken hata:", error);
            } finally {
                setLoading(false);
            }
        };

        loadData();

        return () => {
            if (currentLogoUrl) {
                URL.revokeObjectURL(currentLogoUrl);
            }
        };
    }, []);

    const fetchSettings = useCallback(async () => {
        try {
            setLoading(true);
            const response = await CompanySettingsService.getCompanySettings();
            setSettings(response.data);
            setLogoUrl(CompanySettingsService.getLogoUrl());
        } catch (error) {
            toast.error('Şirket ayarları getirilirken bir hata oluştu.');
            console.error(error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchSettings();
    }, [fetchSettings]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setSettings(prev => ({ ...prev, [name]: value }));
    };

    const handleInfoSave = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        try {
            await CompanySettingsService.updateCompanyInfo(settings);
            toast.success('Şirket bilgileri başarıyla güncellendi.');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Bilgiler güncellenirken bir hata oluştu.');
        } finally {
            setIsSaving(false);
        }
    };

    const handleLogoSelect = (e) => {
        const file = e.target.files[0];
        if (file) {
            setSelectedLogo(file);
            setPreviewLogo(URL.createObjectURL(file));
        }
    };

    const handleLogoUpload = async () => {
        if (!selectedLogo) return;
        setIsSaving(true);
        try {
            await CompanySettingsService.updateCompanyLogo(selectedLogo);
            const logoResponse = await CompanySettingsService.getLogoUrl();
            const newLogoUrl = URL.createObjectURL(logoResponse.data);
            setLogoUrl(newLogoUrl);

            setSelectedLogo(null);
            setPreviewLogo('');
        } catch (error) {
            console.error(error);
        } finally {
            setIsSaving(false);
        }
    };

    if (loading) {
        return <div className="p-8 text-center">Ayarlar yükleniyor...</div>;
    }

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <h1 className="text-3xl font-bold mb-6">Şirket Ayarları</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div className="md:col-span-2 bg-white p-6 rounded-lg shadow-md">
                    <form onSubmit={handleInfoSave} className="space-y-6">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                            <div>
                                <Label htmlFor="name">Şirket Adı</Label>
                                <Input id="name" name="name" value={settings.name} onChange={handleInputChange} required />
                            </div>
                            <div>
                                <Label htmlFor="email">Şirket E-postası</Label>
                                <Input id="email" name="email" type="email" value={settings.email} onChange={handleInputChange} required />
                            </div>
                            <div>
                                <Label htmlFor="phoneNumber">Telefon Numarası</Label>
                                <Input id="phoneNumber" name="phoneNumber" value={settings.phoneNumber} onChange={handleInputChange} />
                            </div>
                            <div>
                                <Label htmlFor="website">Web Sitesi</Label>
                                <Input id="website" name="website" value={settings.website} onChange={handleInputChange} />
                            </div>
                        </div>
                        <div>
                            <Label htmlFor="address">Adres</Label>
                            <Textarea id="address" name="address" value={settings.address} onChange={handleInputChange} rows={3} />
                        </div>
                        <div>
                            <Label htmlFor="emailFooter">E-posta Alt Bilgisi (Footer)</Label>
                            <Textarea id="emailFooter" name="emailFooter" value={settings.emailFooter} onChange={handleInputChange} rows={4} placeholder="Tüm e-postaların altına eklenecek metin..." />
                        </div>
                        <div className="flex justify-end">
                            <Button type="submit" disabled={isSaving}>
                                <Save className="mr-2 h-4 w-4" />
                                {isSaving ? 'Kaydediliyor...' : 'Bilgileri Kaydet'}
                            </Button>
                        </div>
                    </form>
                </div>

                <div className="bg-white p-6 rounded-lg shadow-md flex flex-col items-center">
                    <h2 className="text-lg font-semibold mb-4">Şirket Logosu</h2>
                    <div className="w-48 h-48 mb-4 p-4 border rounded-full flex items-center justify-center overflow-hidden bg-gray-100">
                        <img
                            src={previewLogo || logoUrl}
                            alt="Şirket Logosu"
                            className="w-full h-full object-contain"
                            onError={(e) => { e.target.onerror = null; e.target.src="https://placehold.co/192x192/f1f5f9/64748b?text=Logo"; }}
                        />
                    </div>
                    <Input
                        id="logo-upload"
                        type="file"
                        className="hidden"
                        onChange={handleLogoSelect}
                        accept="image/png, image/jpeg, image/gif"
                    />
                    <Label htmlFor="logo-upload" className="cursor-pointer inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 px-4 py-2 w-full mb-2">
                        <Upload className="mr-2 h-4 w-4" />
                        Logo Seç
                    </Label>
                    {selectedLogo && (
                        <Button onClick={handleLogoUpload} disabled={isSaving} className="w-full">
                            {isSaving ? 'Yükleniyor...' : 'Logoyu Yükle ve Kaydet'}
                        </Button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CompanySettingsPage;

