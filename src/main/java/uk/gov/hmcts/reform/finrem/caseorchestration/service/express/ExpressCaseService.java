package uk.gov.hmcts.reform.finrem.caseorchestration.service.express;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXPRESS_CASE_PARTICIPATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.DOES_NOT_QUALIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.CONTESTED_VARIATION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;

@Service
@RequiredArgsConstructor
public class ExpressCaseService {

    @Value("${finrem.expressCase.frcs}")
    private List<String> expressCaseFrcs;

    private final FeatureToggleService featureToggleService;

    public void setExpressCaseEnrollmentStatus(FinremCaseData caseData) {
        caseData.setExpressCaseParticipation(qualifiesForExpress(caseData) ? ENROLLED : DOES_NOT_QUALIFY);
    }

    /**
     * Checks if the case is enrolled in the express case pilot.
     *
     * @param caseDetails the legacy case details
     * @return true if the case is enrolled in the express case pilot and the express pilot feature is enabled, false otherwise
     */
    public boolean isExpressCase(CaseDetails caseDetails) {
        ExpressCaseParticipation expressCaseParticipation = Optional.ofNullable(caseDetails.getData().get(EXPRESS_CASE_PARTICIPATION))
            .map(Object::toString)
            .map(ExpressCaseParticipation::forValue)
            .orElse(DOES_NOT_QUALIFY);

        return featureToggleService.isExpressPilotEnabled()
            && ExpressCaseParticipation.ENROLLED.equals(expressCaseParticipation);
    }

    /**
     * Determines if the case qualifies for express case participation based on several conditions.
     *  - is within a participating FRC
     *  - is matrimonial application
     *  - has asset value under 250k
     *  - does not include variation order
     *  - is not marked for fast track procedure.
     *
     * @param caseData the case data
     * @return true if the case qualifies for express case participation, false otherwise
     */
    private boolean qualifiesForExpress(FinremCaseData caseData) {
        String frcValue = caseData.getSelectedAllocatedCourt();
        List<NatureApplication> natureOfApplicationCheckList = caseData
            .getNatureApplicationWrapper().getNatureOfApplicationChecklist();
        Schedule1OrMatrimonialAndCpList typeOfApplication = caseData.getScheduleOneWrapper().getTypeOfApplication();
        EstimatedAssetV2 assetValue = caseData.getEstimatedAssetsChecklistV2();

        return expressCaseFrcs.contains(frcValue)
            && MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.equals(typeOfApplication)
            && UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS.equals(assetValue)
            && !ListUtils.emptyIfNull(natureOfApplicationCheckList).contains(CONTESTED_VARIATION_ORDER)
            && YesOrNo.isNoOrNull(caseData.getFastTrackDecision());
    }
}
