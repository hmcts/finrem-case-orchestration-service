package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContestedUploadedDocument {
    @JsonProperty("caseDocuments")
    private CaseDocument caseDocuments;

    @JsonProperty("caseDocumentType")
    private String caseDocumentType;

    @JsonProperty("caseDocumentParty")
    private String caseDocumentParty;

    @JsonProperty("caseDocumentFdr")
    private String caseDocumentFdr;

    @JsonProperty("caseDocumentConfidential")
    private String caseDocumentConfidential;

    @JsonProperty("caseDocumentOther")
    private String caseDocumentOther;

    @JsonProperty("hearingDetails")
    private String hearingDetails;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime caseDocumentUploadDateTime;
}
