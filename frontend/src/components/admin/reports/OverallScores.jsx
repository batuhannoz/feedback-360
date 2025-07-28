import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/ui/card';

const OverallScores = ({ reportData }) => {
    return (
        <Card className="mb-6">
            <CardHeader>
                <CardTitle>Genel Puanlar</CardTitle>
            </CardHeader>
            <CardContent className="grid grid-cols-3 gap-4">
                <div className="text-center">
                    <p className="text-sm text-muted-foreground">Ham Puan</p>
                    <p className="text-2xl font-bold">{reportData.rawAverageScore.toFixed(2)}</p>
                </div>
                <div className="text-center">
                    <p className="text-sm text-muted-foreground">Yetkinlik Ağırlıklı Puan</p>
                    <p className="text-2xl font-bold">{reportData.competencyWeightedScore.toFixed(2)}</p>
                </div>
                <div className="text-center">
                    <p className="text-sm text-muted-foreground">Nihai Puan</p>
                    <p className="text-2xl font-bold">{reportData.finalWeightedScore.toFixed(2)}</p>
                </div>
            </CardContent>
        </Card>
    );
};

export default OverallScores;
