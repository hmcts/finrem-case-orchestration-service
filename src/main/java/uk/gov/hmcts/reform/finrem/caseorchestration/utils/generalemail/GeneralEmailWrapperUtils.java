package uk.gov.hmcts.reform.finrem.caseorchestration.utils.generalemail;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

/**
 * Utility class for handling Refuge Wrapper updates in case data.
 */
@Slf4j
public class GeneralEmailWrapperUtils {

    private GeneralEmailWrapperUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Sets the working attributes of GeneralEmailWrapper to null.
     * This is so that a fresh Create General Email event will show the values as blank in the frontend.
     *
     * @param caseDetails the {@code CaseDetails} object containing case-specific details like the case ID.
     *
     * @throws NullPointerException if {@code caseDetails} is null.
     */
    public static void setGeneralEmailValuesToNull(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        caseData.getGeneralEmailWrapper().setGeneralEmailRecipient(null);
        caseData.getGeneralEmailWrapper().setGeneralEmailCreatedBy(null);
        caseData.getGeneralEmailWrapper().setGeneralEmailUploadedDocument(null);
        caseData.getGeneralEmailWrapper().setGeneralEmailBody(null);
    }
}
