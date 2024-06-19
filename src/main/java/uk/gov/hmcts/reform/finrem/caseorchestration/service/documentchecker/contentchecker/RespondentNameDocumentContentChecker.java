package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

@Component
public class RespondentNameDocumentContentChecker implements DocumentContentChecker {

    private static final String WARNING = "Respondent name may not match";

    @Override
    public String getWarning(FinremCaseDetails caseDetails, String[] content) {
        boolean checkFails = Stream.of(content)
            .filter(this::containsTheRespondentIs)
            .anyMatch(c -> contentNameNotEqualsCaseName(caseDetails.getData(), c));

        return checkFails ? WARNING : null;
    }

    private boolean containsTheRespondentIs(String text) {
        return defaultString(text).contains("The respondent is");
    }

    private boolean contentNameNotEqualsCaseName(FinremCaseData caseData, String content) {
        String respondentName = getRespondentNameFromCase(caseData);
        if (isEmpty(respondentName)) {
            return false;
        }
        return !respondentName.equals(getRespondentNameFromContent(content.trim()));
    }

    private String getRespondentNameFromContent(String text) {
        return text.replace("The respondent is ", "").trim();
    }

    private String getRespondentNameFromCase(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetails = caseData.getContactDetailsWrapper();
        return trim(trim(ofNullable(contactDetails.getRespondentFmName()).orElse(StringUtils.EMPTY)) + SPACE
            + trim(ofNullable(contactDetails.getRespondentLname()).orElse(StringUtils.EMPTY)));
    }
}
