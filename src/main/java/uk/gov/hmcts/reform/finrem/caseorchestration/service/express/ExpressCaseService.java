package uk.gov.hmcts.reform.finrem.caseorchestration.service.express;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.DOES_NOT_QUALIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.CONTESTED_VARIATION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;

@Service
@RequiredArgsConstructor
public class ExpressCaseService {

    @Value("${finrem.expressCase.frcs}")
    List<String> expressCaseFrcs;

    public void setExpressCaseEnrollmentStatus(FinremCaseData caseData) {
        caseData.setExpressCaseParticipation(qualifiesForExpress(caseData) ? ENROLLED : DOES_NOT_QUALIFY);
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
            && typeOfApplication.equals(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS)
            && assetValue.equals(UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS)
            && !natureOfApplicationCheckList.contains(CONTESTED_VARIATION_ORDER)
            && caseData.getFastTrackDecision().isNoOrNull();
    }
}
