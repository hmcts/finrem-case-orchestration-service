package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ManageCaseDocumentsAction", generate = true)
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum ManageCaseDocumentsAction {
    @CCD(label = "Add New")
    ADD_NEW("Add_new"),
    AMEND("Amend");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
