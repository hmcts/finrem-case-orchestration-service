package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

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
    private YesOrNo caseDocumentConfidentiality;
    private String hearingDetails;
    private YesOrNo caseDocumentFdr;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime caseDocumentUploadDateTime;

    public UploadCaseDocument(UploadCaseDocument uploadCaseDocument) {
        this.setCaseDocuments(new CaseDocument(uploadCaseDocument.getCaseDocuments()));
        this.setCaseDocumentType(uploadCaseDocument.getCaseDocumentType());
        this.setCaseDocumentParty(uploadCaseDocument.getCaseDocumentParty());
        this.setCaseDocumentOther(uploadCaseDocument.getCaseDocumentOther());
        this.setCaseDocumentConfidentiality(uploadCaseDocument.getCaseDocumentConfidentiality());
        this.setHearingDetails(uploadCaseDocument.getHearingDetails());
        this.setCaseDocumentFdr(uploadCaseDocument.getCaseDocumentFdr());
        this.setCaseDocumentUploadDateTime(uploadCaseDocument.getCaseDocumentUploadDateTime());
    }
}
