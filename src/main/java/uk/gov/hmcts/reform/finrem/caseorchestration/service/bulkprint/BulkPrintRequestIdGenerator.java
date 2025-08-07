package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

public class BulkPrintRequestIdGenerator {

    private static final String REQUEST_ID_FORMAT = "%s:%s:%s";

    private BulkPrintRequestIdGenerator() {
        // All access to this class should be static
    }

    /**
     * Generates a request ID for bulk print requests that is unique for the current CCD event and recipient party.
     * @param caseDetails the case details
     * @param recipientParty the recipient party (e.g., "applicant", "respondent")
     * @return a request ID string
     */
    public static String generate(FinremCaseDetails caseDetails, String recipientParty) {
        return String.format(REQUEST_ID_FORMAT,
            caseDetails.getId(),
            recipientParty,
            caseDetails.getVersion());
    }
}
