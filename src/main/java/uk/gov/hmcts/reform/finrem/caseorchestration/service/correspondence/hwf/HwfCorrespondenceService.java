package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

@Service
@RequiredArgsConstructor
@Slf4j
public class HwfCorrespondenceService {

    private final CaseDataService caseDataService;

    private final HwfConsentedApplicantCorresponder hwfConsentedApplicantCorresponder;

    private final HwfContestedApplicantCorresponder hwfContestedApplicantCorresponder;

    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        log.info("Send HWF correspondence for Case ID: {}", caseDetails.getId());
        if (caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Send HWF Consented correspondence for Case ID: {}", caseDetails.getId());
            hwfConsentedApplicantCorresponder.sendCorrespondence(caseDetails, authToken);
        } else if (caseDataService.isContestedApplication(caseDetails)) {
            log.info("Send HWF Contested correspondence for Case ID: {}", caseDetails.getId());
            hwfContestedApplicantCorresponder.sendCorrespondence(caseDetails);
        }
    }

}
