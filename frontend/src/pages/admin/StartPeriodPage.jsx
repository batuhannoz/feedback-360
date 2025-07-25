import React from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import evaluationPeriodService from '../../services/evaluationPeriodService';
import { toast } from 'react-toastify';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/card';
import { Rocket, CheckCircle } from 'lucide-react';

const StartPeriodPage = () => {
    const selectedPeriod = useSelector((state) => state.period.selectedPeriod);
    const navigate = useNavigate();

    const handleStartPeriod = () => {
        if (!selectedPeriod) {
            toast.error('Lütfen bir dönem seçin.');
            return;
        }

        const request = { status: 'IN_PROGRESS' };

        evaluationPeriodService.updatePeriodStatus(selectedPeriod.id, request)
            .then(() => {
                toast.success('Değerlendirme dönemi başarıyla başlatıldı!');
                navigate('/dashboard');
            })
            .catch(error => {
                toast.error('Dönem başlatılırken bir hata oluştu.');
                console.error('Error starting period:', error);
            });
    };

    if (!selectedPeriod) {
        return (
            <div className="p-8 text-center">
                <p>İşlem yapmak için lütfen bir değerlendirme dönemi seçin.</p>
            </div>
        );
    }

    if (selectedPeriod.status === 'IN_PROGRESS') {
        return (
            <div className="p-8 flex justify-center items-center bg-gray-50 min-h-screen">
                <Card className="w-full max-w-2xl text-center">
                    <CardHeader>
                        <div className="mx-auto bg-green-500 text-white rounded-full h-16 w-16 flex items-center justify-center">
                            <CheckCircle className="h-8 w-8" />
                        </div>
                        <CardTitle className="mt-4 text-2xl">Dönem Zaten Aktif</CardTitle>
                        <CardDescription className="mt-2">
                            '{selectedPeriod.periodName}' adlı değerlendirme dönemi zaten devam ediyor.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-gray-600 mb-6">
                            Bu dönem için başlatma işlemi daha önce yapılmış. Süreci takip etmek veya sonuçları görüntülemek için panele dönebilirsiniz.
                        </p>
                        <Button onClick={() => navigate('/dashboard')} size="lg" variant="outline">
                            Panele Geri Dön
                        </Button>
                    </CardContent>
                </Card>
            </div>
        );
    }

    return (
        <div className="p-8 flex justify-center items-center bg-gray-50 min-h-screen">
            <Card className="w-full max-w-2xl text-center">
                <CardHeader>
                    <div className="mx-auto bg-primary text-primary-foreground rounded-full h-16 w-16 flex items-center justify-center">
                        <Rocket className="h-8 w-8" />
                    </div>
                    <CardTitle className="mt-4 text-2xl">Değerlendirme Dönemini Başlat</CardTitle>
                    <CardDescription className="mt-2">
                        '{selectedPeriod.periodName}' dönemini başlatmak üzeresiniz. Bu işlem geri alınamaz.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <p className="text-gray-600 mb-6">
                        Dönemi başlattığınızda, tüm katılımcılara değerlendirme anketleri gönderilecek ve süreç başlayacaktır. Lütfen tüm ayarların doğru olduğundan emin olun.
                    </p>
                    <Button onClick={handleStartPeriod} size="lg">
                        Dönemi Başlat
                    </Button>
                </CardContent>
            </Card>
        </div>
    );
};

export default StartPeriodPage;