package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_s_documentType", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum AdditionalDocumentType {
    @CCD(label = "Statement in support (including MPS)")
    STATEMENT_IN_SUPPORT_INCLUDING_MPS("statementInsupportIncludingMPS"),
    @CCD(label = "Schedule of assets")
    SCHEDULE_OF_ASSETS("scheduleOfAssets"),
    @CCD(label = "Letter")
    LETTER("letter"),
    @CCD(label = "Notice of acting")
    NOTICE_OF_ACTING("noticeOfActing"),
    @CCD(label = "Allocation Questionnaire")
    ALLOCATION_QUESTIONNAIRE("allocationQuestionnaire"),
    @CCD(label = "Other")
    OTHER("other");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static AdditionalDocumentType forValue(String value) {
        return Arrays.stream(AdditionalDocumentType.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
