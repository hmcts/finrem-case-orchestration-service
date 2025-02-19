package uk.gov.hmcts.reform.finrem.caseorchestration.service.expresspilot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressPilotParticipation.DOES_NOT_QUALIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressPilotParticipation.ENROLLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.CONTESTED_VARIATION_ORDER;

@Service
@RequiredArgsConstructor
public class ExpressPilotService {

    @Value("${finrem.expressPilot.frcs}")
    List<String> expressPilotFrcs;

    public void setPilotEnrollmentStatus(FinremCaseData caseData) {
        caseData.setExpressPilotParticipation(qualifiesForExpress(caseData) ? ENROLLED : DOES_NOT_QUALIFY);
    }

    private boolean qualifiesForExpress(FinremCaseData caseData) {
        Optional<String> frcValue = Optional.ofNullable(caseData.getSelectedAllocatedCourt());
        List<NatureApplication> natureOfApplicationCheckList = caseData
            .getNatureApplicationWrapper().getNatureOfApplicationChecklist();

        return frcValue.isPresent()
            && expressPilotFrcs.contains(frcValue.get())
            && caseData.getEstimatedAssetsChecklistV2().equals(UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS)
            && !natureOfApplicationCheckList.contains(CONTESTED_VARIATION_ORDER)
            && caseData.getFastTrackDecision().isNoOrNull();
    }
}
