package uk.gov.hmcts.reform.finrem.caseorchestration.service.express;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.DOES_NOT_QUALIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.CONTESTED_VARIATION_ORDER;

@Service
@RequiredArgsConstructor
public class ExpressCaseService {

    @Value("${finrem.expressCase.frcs}")
    List<String> expressCaseFrcs;

    public void setExpressCaseEnrollmentStatus(FinremCaseData caseData) {
        caseData.setExpressCaseParticipation(qualifiesForExpress(caseData) ? ENROLLED : DOES_NOT_QUALIFY);
    }

    private boolean qualifiesForExpress(FinremCaseData caseData) {
        String frcValue = caseData.getSelectedAllocatedCourt();
        List<NatureApplication> natureOfApplicationCheckList = caseData
            .getNatureApplicationWrapper().getNatureOfApplicationChecklist();

        return expressCaseFrcs.contains(frcValue)
            && caseData.getEstimatedAssetsChecklistV2().equals(UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS)
            && !natureOfApplicationCheckList.contains(CONTESTED_VARIATION_ORDER)
            && caseData.getFastTrackDecision().isNoOrNull();
    }
}
