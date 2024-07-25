package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import com.google.common.base.CharMatcher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Component
public class CaseNumberDocumentContentChecker implements DocumentContentChecker {

    private static final String WARNING = "Case numbers may not match";

    @Override
    public String getWarning(FinremCaseDetails caseDetails, String[] content) {
        boolean checkFails = Stream.of(content)
            .filter(this::containsCaseNumber)
            .anyMatch(c -> contentCaseNumberNotEqualsCaseNumber(caseDetails, c));

        return checkFails ? WARNING : null;
    }

    private boolean containsCaseNumber(String text) {
        return defaultString(text).toLowerCase().contains("case number")
            || defaultString(text).toLowerCase().contains("case no")
            || defaultString(text).toLowerCase().contains("reference number");
    }

    private boolean contentCaseNumberNotEqualsCaseNumber(FinremCaseDetails caseDetails, String content) {
        String caseNumberFromContent = getCaseNumberFromContent(content);
        if (caseDetails.getId() == null) {
            return false;
        }
        return !String.valueOf(caseDetails.getId()).equals(caseNumberFromContent);
    }

    private String getCaseNumberFromContent(String text) {
        return defaultString(CharMatcher.inRange('0', '9').retainFrom(text));
    }

}
