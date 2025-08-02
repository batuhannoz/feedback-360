package com.batuhan.feedback360.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportDisplaySettings {

    private String competencyDefinitionTitle; // "Yetkinlik Nedir?"
    private String competencyDefinitionText;
    private String competencySourceWeightsTitle; // "Yetkinlik Bazlı Kaynak Ağırlıkları"
    private String competencySourceWeightsText;
    private String sourcesTitle; // "Kaynaklar"
    private String sourcesText;

    private boolean showCompetencyFormWeights; // Yetkinlik Bazlı Form Ağırlıkları
    private boolean showCompetencySourceWeights; // Yetkinlik Bazlı Kaynak Ağırlıkları
    private boolean showSources; // Kaynaklar
    private boolean showOverallResults; // Genel Değerlendirme Sonuçları
    private boolean showSourceBasedCompetencyScores; // Kaynak Bazlı Yetkinlik Puanları
    private boolean showDetailedCompetencyScores; // Detaylı Yetkinlik Puanları
    private boolean showCommentsSection; // Yorumlar Bölümü
    private boolean showOpinionsSection; // Görüşler Bölümü

    private boolean showOverallRawScore; // Genel Ham Puan
    private boolean showOverallWeightedScore; // Genel Ağırlıklı Puan
    private boolean showDetailedGraph; // Detaylı Yetkinlik -> Grafik
    private boolean showDetailedQuestions; // Detaylı Yetkinlik -> Sorular
    private boolean showDetailedQuestionDescription; // Soru Açıklaması
    private boolean showDetailedCompetencyRawScore; // Detaylı Yetkinlik -> Ham Puan
    private boolean showDetailedCompetencyWeightedScore; // Detaylı Yetkinlik -> Ağırlıklı Puan
    private boolean showCommentSourceName; // Yorumlar -> Kaynak Adı

    private boolean showSelfColumn; // Kendi
    private boolean showPeerColumn; // Eş Değer
    private boolean showManagerColumn; // Yönetici
    private boolean showSubordinateColumn; // Ast
    private boolean showOtherColumn; // Diğer
    private boolean showAverageColumn; // Ortalama
}