package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@Service
@Slf4j
@RequiredArgsConstructor
public class StopRepresentingClientLetterService {

    private final GenericDocumentService genericDocumentService;

    private final DocumentConfiguration documentConfiguration;

    private final LetterDetailsMapper letterDetailsMapper;

    /**
     * Generates a "Stop Representing" letter for the applicant.
     *
     * <p>Uses the configured applicant template and delegates to
     * {@link #generateStopRepresentingLetter(FinremCaseDetails, String, DocumentHelper.PaperNotificationRecipient, String, String)}.
     * The returned {@link CaseDocument} contains the generated PDF letter notifying that
     * the applicant's representation has been removed.</p>
     *
     * @param finremCaseDetails the case details for generating the letter
     * @param authorisationToken the user's authorisation token for document generation
     * @return a {@link CaseDocument} containing the generated letter to the applicant
     */
    public CaseDocument generateStopRepresentingApplicantLetter(FinremCaseDetails finremCaseDetails,
                                                                String authorisationToken) {
        return generateStopRepresentingLetter(
            finremCaseDetails,
            authorisationToken,
            APPLICANT,
            "ApplicantRepresentationRemovalNotice",
            documentConfiguration.getStopRepresentingLetterToApplicantTemplate()
        );
    }

    /**
     * Generates a "Stop Representing" letter for the respondent.
     *
     * <p>Uses the configured respondent template and delegates to
     * {@link #generateStopRepresentingLetter(FinremCaseDetails, String, DocumentHelper.PaperNotificationRecipient, String, String)}.
     * The returned {@link CaseDocument} contains the generated PDF letter notifying that
     * the respondent's representation has been removed.</p>
     *
     * @param finremCaseDetails the case details for generating the letter
     * @param authorisationToken the user's authorisation token for document generation
     * @return a {@link CaseDocument} containing the generated letter to the respondent
     */
    public CaseDocument generateStopRepresentingRespondentLetter(FinremCaseDetails finremCaseDetails,
                                                                 String authorisationToken) {
        return generateStopRepresentingLetter(
            finremCaseDetails,
            authorisationToken,
            RESPONDENT,
            "RespondentRepresentationRemovalNotice",
            documentConfiguration.getStopRepresentingLetterToRespondentTemplate()
        );
    }

    /**
     * Internal helper method to generate a "Stop Representing" letter for a given recipient.
     *
     * <p>Builds a {@link CaseDocument} using the provided template and recipient details.
     * The filename includes a timestamp to ensure uniqueness. The actual PDF generation
     * is handled by {@link GenericDocumentService#generateDocumentFromPlaceholdersMap(String, Map, String, String, CaseType)}.</p>
     *
     * @param finremCaseDetails the case details used to populate the letter
     * @param authorisationToken the user's authorisation token for document generation
     * @param recipient the recipient of the letter (e.g., applicant or respondent)
     * @param filenamePrefix the prefix for the generated PDF filename
     * @param template the document template identifier to use
     * @return a {@link CaseDocument} containing the generated "Stop Representing" letter
     */

    private CaseDocument generateStopRepresentingLetter(FinremCaseDetails finremCaseDetails,
                                                        String authorisationToken,
                                                        DocumentHelper.PaperNotificationRecipient recipient,
                                                        String filenamePrefix,
                                                        String template) {
        Map<String, Object> documentDataMap =
            letterDetailsMapper.getLetterDetailsAsMap(finremCaseDetails, recipient);

        String documentFilename = format("%s_%s.pdf",
            filenamePrefix,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            documentDataMap,
            template,
            documentFilename,
            finremCaseDetails.getCaseType()
        );
    }
}
