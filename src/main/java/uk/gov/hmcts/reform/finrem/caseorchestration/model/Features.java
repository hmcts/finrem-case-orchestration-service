package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    RESPONDENT_JOURNEY("respondent_journey"),
    SHARE_A_CASE("share_a_case"),
    SEND_TO_FRC("send_to_frc");

    private final String name;
}
