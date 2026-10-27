package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AccessCodeGenerator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient;

@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenPinInPostService {

    /**
     * Ensures a PIN is generated for an eligible Citizen UI party
     * in a contested Financial Remedy case.
     *
     * @return true if a new PIN was generated, otherwise false
     */
    public boolean ensurePinForRecipient(FinremCaseDetails caseDetails,
                                         PaperNotificationRecipient recipient) {

        if (caseDetails.getCaseType() != CaseType.CONTESTED) {
            return false;
        }

        boolean generated = AccessCodeGenerator
            .ensureAccessCodeForRecipient(caseDetails.getData(),
                recipient
            );

        if (generated) {
            log.info(
                "Generated Citizen UI PIN for {} on case {}",
                recipient,
                caseDetails.getId()
            );
        }

        return generated;
    }
}
