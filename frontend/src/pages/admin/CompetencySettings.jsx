import CompetencyWeightsPage from "./CompetencyWeightsPage.jsx";
import EvaluationScalePage from "./EvaluationsScalePage.jsx";

const CompetencySettings = () => {
    return (
        <div className="px-5 pt-1 bg-gray-50 min-h-screen">
            <CompetencyWeightsPage />
            <EvaluationScalePage />
        </div>
    )
}

export default CompetencySettings;
