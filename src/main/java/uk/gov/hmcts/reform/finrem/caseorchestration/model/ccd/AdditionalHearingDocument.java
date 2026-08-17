package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_AdditionalHearingCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalHearingDocument implements HasCaseDocument {
    @CCD(
            label = "Additional Hearing Document",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("additionalHearingDocument")
    private CaseDocument document;

    @CCD(label = "Upload DateTime", searchable = false)
    @JsonProperty("additionalHearingDocumentDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime additionalHearingDocumentDate;
}
