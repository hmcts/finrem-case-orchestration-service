package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasSubmittedInfo;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrderUtils.consolidateUploadingDocuments;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuggestedDraftOrder implements HasCaseDocument, HasSubmittedInfo, HasUploadingDocuments {
    private CaseDocument draftOrder;
    private CaseDocument pensionSharingAnnex;
    private String submittedBy;
    private String submittedByEmail;
    private String uploadedOnBehalfOf;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;
    private List<DocumentCollection> attachments;

    @Override
    @JsonIgnore
    public List<CaseDocument> getUploadingDocuments() {
        return consolidateUploadingDocuments(draftOrder, pensionSharingAnnex, attachments);
    }
}
