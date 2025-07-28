import React from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../../components/ui/card';
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '../../../components/ui/accordion';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../../components/ui/table';

const CompetencyReport = ({ competencies }) => {
    return (
        <Card>
            <CardHeader>
                <CardTitle>Yetkinlik Bazında Rapor</CardTitle>
            </CardHeader>
            <CardContent>
                <Accordion type="single" collapsible className="w-full">
                    {competencies.map(competency => (
                        <AccordionItem key={competency.competencyId} value={`item-${competency.competencyId}`}>
                            <AccordionTrigger>
                                <div className="flex justify-between w-full pr-4">
                                    <span>{competency.competencyTitle}</span>
                                    <span className="font-bold">{competency.finalWeightedScore.toFixed(2)}</span>
                                </div>
                            </AccordionTrigger>
                            <AccordionContent>
                                <div className="mb-4">
                                    <h4 className="font-semibold mb-2">Yorumlar</h4>
                                    {competency.comments.length > 0 ? (
                                        <ul className="list-disc pl-5 space-y-1">
                                            {competency.comments.map((comment, index) => (
                                                <li key={index} className="text-sm">{comment}</li>
                                            ))}
                                        </ul>
                                    ) : (
                                        <p className="text-sm text-muted-foreground">Yorum bulunmamaktadır.</p>
                                    )}
                                </div>
                                <div>
                                    <h4 className="font-semibold mb-2">Soru Bazında Puanlar</h4>
                                    <Table>
                                        <TableHeader>
                                            <TableRow>
                                                <TableHead>Soru</TableHead>
                                                <TableHead>Değerleyici</TableHead>
                                                <TableHead className="text-right">Puan</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {competency.questionScores.map(qScore => (
                                                <React.Fragment key={qScore.questionId}>
                                                    {qScore.scoresByEvaluatorType.map((score, index) => (
                                                        <TableRow key={`${qScore.questionId}-${index}`}>
                                                            {index === 0 && <TableCell rowSpan={qScore.scoresByEvaluatorType.length} className="align-top">{qScore.questionText}</TableCell>}
                                                            <TableCell>{score.evaluatorType}</TableCell>
                                                            <TableCell className="text-right">{score.score.toFixed(2)}</TableCell>
                                                        </TableRow>
                                                    ))}
                                                </React.Fragment>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </div>
                            </AccordionContent>
                        </AccordionItem>
                    ))}
                </Accordion>
            </CardContent>
        </Card>
    );
};

export default CompetencyReport;
