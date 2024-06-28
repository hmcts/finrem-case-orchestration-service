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

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalHearingDocument implements HasCaseDocument {
    @JsonProperty("additionalHearingDocument")
    private CaseDocument document;

    @JsonProperty("additionalHearingDocumentDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime additionalHearingDocumentDate;
}
