package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.stream.Stream;

@Component
public class ApplicantNameDocumentContentChecker implements DocumentContentChecker {

    private static final String WARNING = "Applicant name may not match";

    @Override
    public String getWarning(FinremCaseDetails caseDetails, String[] content) {
        boolean checkFails = Stream.of(content)
            .filter(this::containsTheApplicantIs)
            .anyMatch(c -> contentNameNotEqualsCaseName(caseDetails.getData(), c));

        return checkFails ? WARNING : null;
    }

    private boolean containsTheApplicantIs(String text) {
        return text.contains("1. The applicant is");
    }

    private boolean contentNameNotEqualsCaseName(FinremCaseData caseData, String content) {
        return !getApplicantNameFromCase(caseData).equals(getApplicantNameFromContent(content.trim()));
    }

    private String getApplicantNameFromContent(String text) {
        return text.replace("1. The applicant is ", "").trim();
    }

    private String getApplicantNameFromCase(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetails = caseData.getContactDetailsWrapper();
        return contactDetails.getApplicantFmName() + " " + contactDetails.getApplicantLname();
    }
}
