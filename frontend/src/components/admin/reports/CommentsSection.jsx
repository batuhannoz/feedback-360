import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "../../ui/card.jsx";

// Backend'den gelen enum değerlerini kullanıcı dostu başlıklara çevirelim
const evaluatorTypeTranslations = {
    SELF: 'Kendi Görüşü',
    MANAGER: 'Yönetici Görüşleri',
    PEER: 'Eş Değer Görüşleri',
    SUBORDINATE: 'Ast Görüşleri',
    OTHER: 'Diğer Görüşler'
};

const CommentsSection = ({ comments }) => {
    // Yorum yoksa veya liste boşsa bileşeni render etme
    if (!comments || comments.length === 0) {
        return null;
    }

    // Yorumları değerlendiren tipine göre gruplayalım
    const groupedComments = comments.reduce((acc, comment) => {
        const type = comment.evaluatorType;
        // Eğer bu tip için henüz bir dizi oluşturulmadıysa, oluştur
        if (!acc[type]) {
            acc[type] = [];
        }
        // Yorumu ilgili gruba ekle
        acc[type].push(comment);
        return acc;
    }, {});

    // Grupların gösterileceği sırayı belirleyebiliriz
    const displayOrder = ['SELF', 'MANAGER', 'PEER', 'SUBORDINATE', 'OTHER'];
    const sortedGroupKeys = displayOrder.filter(key => groupedComments[key]);


    return (
        <Card>
            <CardHeader>
                <CardTitle>Görüşler ve Yorumlar</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
                {sortedGroupKeys.length > 0 ? (
                    sortedGroupKeys.map(type => (
                        <div key={type}>
                            <h4 className="text-lg font-semibold mb-3 border-b pb-2">
                                {evaluatorTypeTranslations[type] || 'Diğer Yorumlar'}
                            </h4>
                            <div className="space-y-4">
                                {groupedComments[type].map((comment, index) => (
                                    <blockquote key={index} className="border-l-4 border-gray-300 pl-4 italic text-gray-700">
                                        <p className="mb-1">"{comment.comment}"</p>
                                        {/* Eğer backend ayarlarında kaynak adı gösterimi kapalıysa,
                                            'evaluatorName' alanı null gelecektir.
                                            Bu durumda footer'ı hiç göstermiyoruz.
                                        */}
                                        {comment.evaluatorName && (
                                            <footer className="text-sm not-italic text-gray-500">
                                                - {comment.evaluatorName}
                                            </footer>
                                        )}
                                    </blockquote>
                                ))}
                            </div>
                        </div>
                    ))
                ) : (
                    <p className="text-gray-500">Gösterilecek yorum bulunmamaktadır.</p>
                )}
            </CardContent>
        </Card>
    );
};

export default CommentsSection;
