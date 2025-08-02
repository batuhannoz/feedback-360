import React from 'react';
import {Card, CardContent, CardHeader, CardTitle} from "../../ui/card.jsx";


const OverallScores = ({ scores }) => {
    if (!scores) return null;

    return (
        <Card>
            <CardHeader><CardTitle>Genel Değerlendirme Sonuçları</CardTitle></CardHeader>
            <CardContent>
                {scores.rawAverageScore && <p>Ham Puan: {scores.rawAverageScore}</p>}
                {scores.finalWeightedScore && <p>Ağırlıklı Puan: {scores.finalWeightedScore}</p>}
            </CardContent>
        </Card>
    );
};

export default OverallScores;
