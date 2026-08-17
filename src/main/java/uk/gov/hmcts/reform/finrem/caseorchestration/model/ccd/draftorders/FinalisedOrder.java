package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_finalisedOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FinalisedOrder implements HasCaseDocument, WithAttachments {

    @CCD(label = "Draft order", categoryID = "approvedOrders", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument finalisedDocument;

    @CCD(
            label = "Attachments",
            categoryID = "approvedOrders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    private List<DocumentCollectionItem> attachments;

    @CCD(label = "Cover Letter", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument coverLetter;

    @CCD(label = "Submitted by", searchable = false)
    private String submittedBy;

    @CCD(label = "Date submitted", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;

    @CCD(label = "Approval date", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime approvalDate;

    @CCD(label = "Judge name", searchable = false)
    private String approvalJudge;

    @CCD(label = "Final order", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo finalOrder;

}
