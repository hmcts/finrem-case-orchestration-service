package uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.Optional;

/**
 * Utility class for handling Refuge Wrapper updates in case data.
 */
@Slf4j
public class RefugeWrapperUtils {

    private RefugeWrapperUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Updates the `respondentInRefugeTab` field in the refuge wrapper with the value of
     * `respondentInRefugeQuestion`, logs the update, and then clears the `respondentInRefugeQuestion` field.
     *
     * @param caseDetails the {@code CaseDetails} object containing case-specific details like the case ID.
     *
     * @throws NullPointerException if {@code caseDetails} is null.
     */
    public static void updateRespondentInRefugeTab(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Optional.ofNullable(caseData.getRefugeWrapper().getRespondentInRefugeQuestion())
                .ifPresent(question -> {
                    caseData.getRefugeWrapper().setRespondentInRefugeTab(question);
                    log.info("Updating respondentInRefugeTab for case reference {}. Removing respondentInRefugeQuestion",
                            caseDetails.getId());
                    caseData.getRefugeWrapper().setRespondentInRefugeQuestion(null);
                });
    }

    /**
     * Updates the `applicantInRefugeTab` field in the refuge wrapper with the value of
     * `applicantInRefugeQuestion`, logs the update, and then clears the `applicantInRefugeQuestion` field.
     *
     * @param caseDetails the {@code CaseDetails} object containing case-specific details like the case ID.
     *
     * @throws NullPointerException if {@code caseDetails} is null.
     */
    public static void updateApplicantInRefugeTab(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Optional.ofNullable(caseData.getRefugeWrapper().getApplicantInRefugeQuestion())
                .ifPresent(question -> {
                    caseData.getRefugeWrapper().setApplicantInRefugeTab(question);
                    log.info("Updating applicantInRefugeTab for case reference {}. Removing applicantInRefugeQuestion",
                            caseDetails.getId());
                    caseData.getRefugeWrapper().setApplicantInRefugeQuestion(null);
                });
    }

    /**
     * Populates the `applicantInRefugeQuestion` field in the refuge wrapper with the value of
     * `applicantInRefugeTab`, This is used for events that will update the value for applicantInRefugeTab
     * when submitted. This method simply pre-populates the question with what was answered previously.
     *
     * @param caseDetails the {@code CaseDetails} object containing case-specific details including refuge info.
     *
     * @throws NullPointerException if {@code caseDetails} is null.
     */
    public static void populateApplicantInRefugeQuestion(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Optional.ofNullable(caseData.getRefugeWrapper().getApplicantInRefugeTab())
                .ifPresent(tab -> caseData.getRefugeWrapper().setApplicantInRefugeQuestion(tab));
    }

    /**
     * Populates the `respondentInRefugeQuestion` field in the refuge wrapper with the value of
     * `respondentInRefugeTab`, This is used for events that will update the value for respondentInRefugeTab
     * when submitted. This method simply pre-populates the question with what was answered previously.
     *
     * @param caseDetails the {@code CaseDetails} object containing case-specific details including refuge info.
     *
     * @throws NullPointerException if {@code caseDetails} is null.
     */
    public static void populateRespondentInRefugeQuestion(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Optional.ofNullable(caseData.getRefugeWrapper().getRespondentInRefugeTab())
                .ifPresent(tab -> caseData.getRefugeWrapper().setRespondentInRefugeQuestion(tab));
    }

}
