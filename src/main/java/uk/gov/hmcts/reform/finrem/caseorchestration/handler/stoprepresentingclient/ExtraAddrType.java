package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExtraAddrType {

    APPLICANT("applicant"),
    RESPONDENT("respondent"),
    INTERVENER1("intervener1"),
    INTERVENER2("intervener2"),
    INTERVENER3("intervener3"),
    INTERVENER4("intervener4");

    private final String id;

}
