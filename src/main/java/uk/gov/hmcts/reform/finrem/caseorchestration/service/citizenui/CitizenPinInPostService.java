package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AccessCodeGenerator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient;

/**
 * Handles Citizen UI PIN generation for eligible parties in contested Financial Remedy cases.
 *
 * <p>Delegates to {@link AccessCodeGenerator} to ensure a PIN is generated
 * only for eligible, unrepresented applicants or respondents.
 *
 * <p>This service updates case data in memory. Persistence to CCD is handled
 * by the calling handler or persistence service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenPinInPostService {

    /**
     * Ensures an eligible applicant or respondent has a Citizen UI PIN.
     *
     * <p>PIN generation is restricted to contested cases and is skipped if
     * the party is represented, has previously activated a PIN, or already
     * has an active PIN.
     *
     * @param caseDetails the case containing the party's access codes
     * @param recipient the party requiring a PIN
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
