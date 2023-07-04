package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class UploadCaseDocument {
    private CaseDocument caseDocuments;
    private CaseDocumentType caseDocumentType;
    private CaseDocumentParty caseDocumentParty;
    private String caseDocumentOther;
    private YesOrNo caseDocumentConfidential;
    private String hearingDetails;
    private YesOrNo caseDocumentFdr;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime caseDocumentUploadDateTime;


    @JsonIgnore
    public YesOrNo getCaseDocumentFdr() {
        if (caseDocumentFdr == null) {
            this.caseDocumentFdr = YesOrNo.NO;
        }
        return caseDocumentFdr;
    }

    @JsonIgnore
    public YesOrNo getCaseDocumentConfidential() {
        if (caseDocumentConfidential == null) {
            this.caseDocumentConfidential = YesOrNo.NO;
        }
        return caseDocumentConfidential;
    }
}
