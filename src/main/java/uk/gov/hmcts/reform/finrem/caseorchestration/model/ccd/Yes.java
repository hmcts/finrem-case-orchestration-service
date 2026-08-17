package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_yesOnly", generate = true)
@RequiredArgsConstructor
public enum Yes {
    @CCD(label = "Yes")
    YES("Yes");

    private final String value;

}
