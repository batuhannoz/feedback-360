import React from 'react';
import { Button } from "../../ui/button.jsx";
import {Card, CardContent, CardHeader, CardTitle} from "../../ui/card.jsx";
import {Switch} from "../../ui/switch.jsx";
import {Label} from "../../ui/label.jsx";

const SettingsGroup = ({ title, children }) => (
    <div className="mb-4">
        <h4 className="font-semibold mb-2 text-md">{title}</h4>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {children}
        </div>
    </div>
);

const SettingToggle = ({ id, label, checked, onCheckedChange }) => (
    <div className="flex items-center space-x-2">
        <Switch id={id} checked={checked} onCheckedChange={onCheckedChange} />
        <Label htmlFor={id} className="text-sm">{label}</Label>
    </div>
);

const ReportSettingsPanel = ({ settings, onSettingsChange, onGenerateReport }) => {

    const handleToggle = (key) => {
        onSettingsChange(prev => ({ ...prev, [key]: !prev[key] }));
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>Rapor Görüntüleme Ayarları</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="space-y-6">
                    <SettingsGroup title="Genel Bölümler">
                        <SettingToggle id="showOverallResults" label="Genel Sonuçlar" checked={settings.showOverallResults} onCheckedChange={() => handleToggle('showOverallResults')} />
                        <SettingToggle id="showSourceBasedCompetencyScores" label="Kaynak Bazlı Puanlar (Grafik)" checked={settings.showSourceBasedCompetencyScores} onCheckedChange={() => handleToggle('showSourceBasedCompetencyScores')} />
                        <SettingToggle id="showDetailedCompetencyScores" label="Detaylı Yetkinlik Puanları" checked={settings.showDetailedCompetencyScores} onCheckedChange={() => handleToggle('showDetailedCompetencyScores')} />
                        <SettingToggle id="showCommentsSection" label="Yorumlar" checked={settings.showCommentsSection} onCheckedChange={() => handleToggle('showCommentsSection')} />
                    </SettingsGroup>
                    <hr/>
                    <SettingsGroup title="Detaylı Yetkinlik Ayarları">
                        <SettingToggle id="showDetailedGraph" label="Grafik" checked={settings.showDetailedGraph} onCheckedChange={() => handleToggle('showDetailedGraph')} />
                        <SettingToggle id="showDetailedQuestions" label="Sorular" checked={settings.showDetailedQuestions} onCheckedChange={() => handleToggle('showDetailedQuestions')} />
                        <SettingToggle id="showDetailedCompetencyRawScore" label="Ham Puan" checked={settings.showDetailedCompetencyRawScore} onCheckedChange={() => handleToggle('showDetailedCompetencyRawScore')} />
                        <SettingToggle id="showDetailedCompetencyWeightedScore" label="Ağırlıklı Puan" checked={settings.showDetailedCompetencyWeightedScore} onCheckedChange={() => handleToggle('showDetailedCompetencyWeightedScore')} />
                    </SettingsGroup>
                    <hr/>
                    <SettingsGroup title="Tablo Sütunları">
                        <SettingToggle id="showSelfColumn" label="Kendi" checked={settings.showSelfColumn} onCheckedChange={() => handleToggle('showSelfColumn')} />
                        <SettingToggle id="showPeerColumn" label="Eş Değer" checked={settings.showPeerColumn} onCheckedChange={() => handleToggle('showPeerColumn')} />
                        <SettingToggle id="showManagerColumn" label="Yönetici" checked={settings.showManagerColumn} onCheckedChange={() => handleToggle('showManagerColumn')} />
                        <SettingToggle id="showSubordinateColumn" label="Ast" checked={settings.showSubordinateColumn} onCheckedChange={() => handleToggle('showSubordinateColumn')} />
                        <SettingToggle id="showOtherColumn" label="Diğer" checked={settings.showOtherColumn} onCheckedChange={() => handleToggle('showOtherColumn')} />
                        <SettingToggle id="showAverageColumn" label="Ortalama" checked={settings.showAverageColumn} onCheckedChange={() => handleToggle('showAverageColumn')} />
                    </SettingsGroup>
                </div>
                <div className="mt-6 flex justify-end">
                    <Button onClick={onGenerateReport}>Raporu Güncelle</Button>
                </div>
            </CardContent>
        </Card>
    );
};

export default ReportSettingsPanel;