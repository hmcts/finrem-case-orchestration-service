package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

/**
 * Stores details of emails that have been sent as part of the case workflow.
 * This holder is used to persist email metadata and associated uploaded documents,
 * including recipients, sender information, email body content, sent date,
 * and any attachments related to the email communication.
 */
@ComplexType(name = "FR_GeneralEmailDocument", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralEmailHolder implements HasCaseDocument {

    @CCD(label = "Recipient's email", searchable = false, typeOverride = FieldType.Email)
    private String generalEmailRecipient;
    @CCD(label = "Email created by", searchable = false)
    private String generalEmailCreatedBy;
    @CCD(label = "Body", searchable = false, typeOverride = FieldType.TextArea)
    private String generalEmailBody;
    @CCD(label = "Upload Document", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument generalEmailUploadedDocument;
    @CCD(
            label = "Upload Document(s)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    private List<DocumentCollectionItem> generalEmailUploadedDocuments;
    @CCD(label = "Date Sent", searchable = false)
    private LocalDateTime generalEmailDateSent;
}
