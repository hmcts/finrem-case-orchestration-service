package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AccessCodeGenerator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public boolean generatePinIfNeeded(FinremCaseDetails caseDetails,
                                       PaperNotificationRecipient recipient) {

        if (caseDetails.getCaseType() != CaseType.CONTESTED) {
            return false;
        }

        boolean generated = AccessCodeGenerator
            .generateAccessCodeIfNeeded(caseDetails.getData(),
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

    /**
     * Finds the recipient's access code for postal correspondence.
     *
     * <p>Prefers the first activated entry in collection order,
     * otherwise the first valid entry.
     *
     * @param caseData the case data containing the access-code history
     * @param recipient the applicant or respondent receiving correspondence
     * @return the selected nonblank code, or empty if none is available
     */
    public Optional<String> findAccessCodeForRecipient(FinremCaseData caseData,
                                                       PaperNotificationRecipient recipient) {

        List<AccessCodeCollection> accessCodes = switch (recipient) {
            case APPLICANT -> caseData.getApplicantAccessCodes();
            case RESPONDENT -> caseData.getRespondentAccessCodes();
            default -> List.of();
        };

        if (accessCodes == null) {
            return Optional.empty();
        }

        List<AccessCodeEntry> entries = accessCodes.stream()
            .filter(Objects::nonNull)
            .map(AccessCodeCollection::getValue)
            .filter(Objects::nonNull)
            .filter(entry -> StringUtils.isNotBlank(entry.getAccessCode()))
            .toList();
        return entries.stream()
            .filter(entry -> entry.getUsedAt() != null
                || StringUtils.isNotBlank(entry.getUserIdamID()))
            .findFirst()
            .or(() -> entries.stream()
                .filter(entry -> YesOrNo.YES.equals(entry.getIsValid()))
                .findFirst())
            .map(AccessCodeEntry::getAccessCode)
            .filter(StringUtils::isNotBlank);
    }
}
