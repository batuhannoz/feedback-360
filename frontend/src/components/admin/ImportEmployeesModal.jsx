import React, { useCallback, useState } from 'react';
import { X, UploadCloud } from 'lucide-react';
import { Button } from '../ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';

const ImportEmployeesModal = ({ isOpen, onClose }) => {
    const [isDragOver, setIsDragOver] = useState(false);

    const handleDragOver = useCallback((event) => {
        event.preventDefault();
        setIsDragOver(true);
    }, []);

    const handleDragLeave = useCallback((event) => {
        event.preventDefault();
        setIsDragOver(false);
    }, []);

    const processFile = (file) => {
        console.log(`İçe aktarılmak üzere dosya seçildi: ${file.name}`);
        console.log('Gerçek içe aktarma işlemi burada yapılacak.');
        alert(`Dosya "${file.name}" içe aktarma için hazırlandı. Detaylar için konsolu kontrol edin.`);
        onClose();
    };

    const handleDrop = useCallback((event) => {
        event.preventDefault();
        setIsDragOver(false);
        if (event.dataTransfer.files && event.dataTransfer.files.length > 0) {
            processFile(event.dataTransfer.files[0]);
            event.dataTransfer.clearData();
        }
    }, [onClose]);

    const handleFileSelect = (event) => {
        if (event.target.files && event.target.files.length > 0) {
            processFile(event.target.files[0]);
        }
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
                    <Button variant="ghost" size="icon" onClick={onClose}>
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
                    />
                    <label
                        htmlFor="file-upload"
                        className={`flex flex-col items-center justify-center w-full h-64 border-2 border-dashed rounded-lg cursor-pointer transition-colors ${
                            isDragOver ? 'border-primary bg-primary/10' : 'border-gray-300 hover:border-gray-400 hover:bg-gray-50'
                        }`}
                    >
                        <div className="flex flex-col items-center justify-center pt-5 pb-6">
                            <UploadCloud className={`w-10 h-10 mb-3 ${isDragOver ? 'text-primary' : 'text-gray-400'}`} />
                            <p className="mb-2 text-sm text-gray-500">
                                <span className="font-semibold">Yüklemek için tıklayın</span> veya dosyayı sürükleyip bırakın
                            </p>
                            <p className="text-xs text-gray-500">Excel (.xlsx) veya CSV</p>
                        </div>
                    </label>
                    <p className="mt-4 text-sm text-muted-foreground">
                        Lütfen içe aktarılacak çalışan listesini içeren Excel dosyasını seçin. Dosya formatı için şablonu indirebilirsiniz.
                    </p>
                    <div className="flex justify-end mt-6">
                        <Button variant="outline" onClick={onClose}>İptal</Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};

export default ImportEmployeesModal;
