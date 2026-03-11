package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static java.util.Optional.ofNullable;

@Getter
@Builder
public class StopRepresentingClientInfo {
    boolean invokedByIntervenerBarrister;
    String userAuthorisation;
    FinremCaseDetails caseDetails;
    FinremCaseDetails caseDetailsBefore;

    FinremCaseData getFinremCaseData() {
        return ofNullable(getCaseDetails())
            .map(FinremCaseDetails::getData)
            .orElse(null);
    }

    FinremCaseData getFinremCaseDataBefore() {
        return ofNullable(getCaseDetailsBefore())
            .map(FinremCaseDetails::getData)
            .orElse(null);
    }

    Long getCaseId() {
        return ofNullable(getFinremCaseData())
            .map(FinremCaseData::getCcdCaseId)
            .map(Long::parseLong)
            .orElse(null);
    }
}
