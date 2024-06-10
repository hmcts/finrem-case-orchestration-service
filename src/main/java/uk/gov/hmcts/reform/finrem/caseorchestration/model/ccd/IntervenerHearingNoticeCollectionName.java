package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IntervenerHearingNoticeCollectionName {

    INTV_1("intv1HearingNoticesCollection"),
    INTV_2("intv2HearingNoticesCollection"),
    INTV_3("intv3HearingNoticesCollection"),
    INTV_4("intv4HearingNoticesCollection");

    private final String value;

    public String getValue() {
        return value;
    }
}
