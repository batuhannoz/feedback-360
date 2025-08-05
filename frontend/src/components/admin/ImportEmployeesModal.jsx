import React, { useCallback, useState } from 'react';
import { X, UploadCloud, Loader2 } from 'lucide-react';
import { Button } from '../ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import * as XLSX from 'xlsx';
import { createUsers } from '../../services/userService';

const ImportEmployeesModal = ({ isOpen, onClose, onSuccess }) => {
    const [isDragOver, setIsDragOver] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleDragOver = useCallback((event) => {
        event.preventDefault();
        setIsDragOver(true);
    }, []);

    const handleDragLeave = useCallback((event) => {
        event.preventDefault();
        setIsDragOver(false);
    }, []);

    const processFile = async (file) => {
        if (!file) return;

        const validExtensions = ['.xlsx', '.xls', '.csv'];
        const fileExtension = '.' + file.name.split('.').pop();
        if (!validExtensions.includes(fileExtension)) {
            setError('Geçersiz dosya formatı. Lütfen .xlsx, .xls veya .csv uzantılı bir dosya yükleyin.');
            return;
        }

        setIsLoading(true);
        setError('');

        const reader = new FileReader();
        reader.onload = async (e) => {
            try {
                const data = e.target.result;
                const workbook = XLSX.read(data, { type: 'binary' });
                const sheetName = workbook.SheetNames[0];
                const worksheet = workbook.Sheets[sheetName];
                const json = XLSX.utils.sheet_to_json(worksheet);

                const mappedUsers = json.map(row => ({
                    firstName: row['İsim'] || row['FirstName'],
                    lastName: row['Soyisim'] || row['LastName'],
                    email: row['Email'] || row['EmailAddress'],
                    role: row['Pozisyon'] || row['Title'],
                    isAdmin: ["Active" ,'evet', 'yes', 'true', '1'].includes(String(row['Admin mi?'] || row['isAdmin']).toLowerCase()),
                    isActive: String(row['Statu'], row['Status'] || row['Durum'] || '').toLowerCase() !== 'passive',
                }));

                const validUsers = mappedUsers.filter(user => user.email);

                if (validUsers.length === 0) {
                    throw new Error("Dosyada geçerli bir kullanıcı bulunamadı. Lütfen email alanlarının dolu olduğundan emin olun.");
                }

                await createUsers(validUsers);

                onSuccess();
                onClose();

            } catch (err) {
                console.error('Error processing or uploading file:', err);
                const apiErrorMessage = err.response?.data?.message || err.message;
                setError(`Bir hata oluştu: ${apiErrorMessage}`);
            } finally {
                setIsLoading(false);
            }
        };

        reader.onerror = (err) => {
            console.error('FileReader error:', err);
            setError('Dosya okunurken bir hata oluştu.');
            setIsLoading(false);
        };

        reader.readAsBinaryString(file);
    };

    const handleDrop = useCallback((event) => {
        event.preventDefault();
        setIsDragOver(false);
        if (event.dataTransfer.files && event.dataTransfer.files.length > 0) {
            processFile(event.dataTransfer.files[0]);
            event.dataTransfer.clearData();
        }
    }, [processFile]);

    const handleFileSelect = (event) => {
        if (event.target.files && event.target.files.length > 0) {
            processFile(event.target.files[0]);
        }
        event.target.value = null;
    };

    if (!isOpen) {
        return null;
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm">
            <Card
                className="w-full max-w-lg mx-4"
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
            >
                <CardHeader className="flex flex-row items-center justify-between">
                    <CardTitle>Çalışanları İçe Aktar</CardTitle>
                    <Button variant="ghost" size="icon" onClick={onClose} disabled={isLoading}>
                        <X className="h-5 w-5" />
                    </Button>
                </CardHeader>
                <CardContent>
                    <input
                        type="file"
                        id="file-upload"
                        className="hidden"
                        onChange={handleFileSelect}
                        accept=".xlsx, .xls, .csv"
                        disabled={isLoading}
                    />
                    <label
                        htmlFor="file-upload"
                        className={`flex flex-col items-center justify-center w-full h-64 border-2 border-dashed rounded-lg cursor-pointer transition-colors ${
                            isLoading ? 'cursor-not-allowed bg-gray-100' :
                                isDragOver ? 'border-primary bg-primary/10' : 'border-gray-300 hover:border-gray-400 hover:bg-gray-50'
                        }`}
                    >
                        {isLoading ? (
                            <div className="flex flex-col items-center justify-center">
                                <Loader2 className="w-10 h-10 mb-3 animate-spin text-primary" />
                                <p className="text-sm text-gray-500">Dosya işleniyor, lütfen bekleyin...</p>
                            </div>
                        ) : (
                            <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                <UploadCloud className={`w-10 h-10 mb-3 ${isDragOver ? 'text-primary' : 'text-gray-400'}`} />
                                <p className="mb-2 text-sm text-gray-500">
                                    <span className="font-semibold">Yüklemek için tıklayın</span> veya dosyayı sürükleyip bırakın
                                </p>
                                <p className="text-xs text-gray-500">Excel (.xlsx, .xls) veya CSV</p>
                            </div>
                        )}
                    </label>

                    {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

                    <p className="mt-4 text-sm text-muted-foreground">
                        Lütfen içe aktarılacak çalışan listesini içeren dosyayı seçin. Gerekli sütunlar: <strong>İsim, Soyisim, Email, Pozisyon, Admin mi?</strong>
                    </p>
                    <div className="flex justify-end mt-6 space-x-2">
                        <Button variant="outline" onClick={onClose} disabled={isLoading}>İptal</Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};

export default ImportEmployeesModal;