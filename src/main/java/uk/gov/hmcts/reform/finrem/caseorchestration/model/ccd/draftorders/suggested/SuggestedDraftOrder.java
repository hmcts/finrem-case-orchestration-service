package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasSubmittedInfo;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRUploadParty;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_suggestedDraftOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuggestedDraftOrder implements HasCaseDocument, HasSubmittedInfo {
    @CCD(label = "Draft order", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument draftOrder;
    @CCD(label = "Pension Sharing Annex", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument pensionSharingAnnex;
    @CCD(label = "Submitted by", searchable = false)
    private String submittedBy;
    @CCD(label = "Submitted by (email address)", searchable = false)
    private String submittedByEmail;
    @CCD(
            label = "Uploaded on behalf of",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "FR_uploadParty",
            typeParameterClass = FRUploadParty.class
    )
    private String uploadedOnBehalfOf;
    @CCD(label = "Date submitted", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    @CCD(
            label = "Attachments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    private List<DocumentCollectionItem> attachments;
}
