package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import com.google.common.base.CharMatcher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

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
        return text.contains("Case number");
    }

    private boolean contentCaseNumberNotEqualsCaseNumber(FinremCaseDetails caseDetails, String content) {
        long caseNumberFromContent = getCaseNumberFromContent(content);
        if (caseDetails.getId() == null) {
            return false;
        }
        return caseNumberFromContent != ofNullable(caseDetails.getId()).orElse(0L);
    }

    private long getCaseNumberFromContent(String text) {
        return Long.parseLong(CharMatcher.inRange('0', '9').retainFrom(text));
    }


}
