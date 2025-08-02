import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../../components/ui/table';

const ScoresByEvaluator = ({ scores }) => {
    return (
        <Card>
            <CardHeader>
                <CardTitle>Kaynak Türüne Göre Puanlar</CardTitle>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Kaynak</TableHead>
                            <TableHead className="text-right">Puan</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {scores.map((score, index) => (
                            <TableRow key={index}>
                                <TableCell>{score.evaluatorName ? score.evaluatorName : score.evaluatorType}</TableCell>
                                <TableCell className="text-right">{score.averageScore.toFixed(2)}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
};

export default ScoresByEvaluator;
