import React from 'react';
import {Card, CardContent, CardHeader, CardTitle} from "../../ui/card.jsx";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from "../../ui/accordion.jsx";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "../../ui/table.jsx";

// Puanı formatlamak veya "-" göstermek için yardımcı fonksiyon
const formatScore = (score) => {
    return score != null ? score.toFixed(2) : '-';
};

const CompetencyReport = ({ competencies }) => {
    if (!competencies || competencies.length === 0) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle>Yetkinlik Bazında Rapor</CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="text-sm text-muted-foreground">Gösterilecek yetkinlik verisi bulunmamaktadır.</p>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Detaylı Yetkinlik Puanları</CardTitle>
            </CardHeader>
            <CardContent>
                <Accordion type="single" collapsible className="w-full">
                    {competencies.map(competency => (
                        <AccordionItem key={competency.competencyId} value={`item-${competency.competencyId}`}>
                            <AccordionTrigger>
                                <div className="flex justify-between w-full pr-4 items-center">
                                    <span className="text-left">{competency.competencyTitle}</span>
                                    {/* `weightedScore` artık doğrudan competency nesnesi altında */}
                                    <div className="text-right">
                                        {competency.rawAverageScore != null && (
                                            <span className="text-sm font-normal text-muted-foreground mr-4">
                                                Ham Puan: {formatScore(competency.rawAverageScore)}
                                            </span>
                                        )}
                                        {competency.weightedScore != null && (
                                            <span className="font-bold">
                                                Ağırlıklı Puan: {formatScore(competency.weightedScore)}
                                            </span>
                                        )}
                                    </div>
                                </div>
                            </AccordionTrigger>
                            <AccordionContent>
                                {/* Soru tablosu yeni veri yapısına göre tamamen yenilendi */}
                                {competency.questionScores && competency.questionScores.length > 0 ? (
                                    <div>
                                        <h4 className="font-semibold mb-2">Soru Bazında Puanlar</h4>
                                        <Table>
                                            <TableHeader>
                                                <TableRow>
                                                    <TableHead className="w-[40%]">Davranış Göstergesi (Soru)</TableHead>
                                                    <TableHead className="text-center">Kendi</TableHead>
                                                    <TableHead className="text-center">Yönetici</TableHead>
                                                    <TableHead className="text-center">Eş Değer</TableHead>
                                                    <TableHead className="text-center">Ast</TableHead>
                                                    <TableHead className="text-center">Diğer</TableHead>
                                                    <TableHead className="text-center font-bold">Ortalama</TableHead>
                                                </TableRow>
                                            </TableHeader>
                                            <TableBody>
                                                {competency.questionScores.map(qScore => (
                                                    // Her soru için tek bir satır oluşturuyoruz
                                                    <TableRow key={qScore.questionId}>
                                                        <TableCell className="font-medium">{qScore.questionText}</TableCell>
                                                        {/* Puanlar artık `scores` nesnesinden alınıyor */}
                                                        <TableCell className="text-center">{formatScore(qScore.scores?.selfScore)}</TableCell>
                                                        <TableCell className="text-center">{formatScore(qScore.scores?.managerScore)}</TableCell>
                                                        <TableCell className="text-center">{formatScore(qScore.scores?.peerScore)}</TableCell>
                                                        <TableCell className="text-center">{formatScore(qScore.scores?.subordinateScore)}</TableCell>
                                                        <TableCell className="text-center">{formatScore(qScore.scores?.otherScore)}</TableCell>
                                                        <TableCell className="text-center font-bold">{formatScore(qScore.scores?.averageScore)}</TableCell>
                                                    </TableRow>
                                                ))}
                                            </TableBody>
                                        </Table>
                                    </div>
                                ) : (
                                    <p className="text-sm text-muted-foreground">Bu yetkinliğe ait soru detayı bulunmamaktadır.</p>
                                )}
                            </AccordionContent>
                        </AccordionItem>
                    ))}
                </Accordion>
            </CardContent>
        </Card>
    );
};

export default CompetencyReport;