package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public interface HearingInstructionProcessable extends DocumentMatcher {

    void setAnotherHearingToBeListed(YesOrNo yesOrNo);

    void setHearingType(String hearingType);

    void setAdditionalTime(String additionalTime);

    void setHearingTimeEstimate(String timeEstimate);

    void setOtherListingInstructions(String instructions);
}
