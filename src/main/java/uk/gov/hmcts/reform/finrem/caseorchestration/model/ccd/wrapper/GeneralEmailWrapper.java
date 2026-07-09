package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralEmailWrapper implements HasCaseDocument {
    @TemporaryField
    private String generalEmailRecipient;
    @TemporaryField
    private String generalEmailCreatedBy;
    @TemporaryField
    private String generalEmailBody;
    @TemporaryField
    private List<DocumentCollectionItem> generalEmailUploadedDocuments;

    // It stores the emails sent
    private List<GeneralEmailCollection> generalEmailCollection;

    /**
     * Returns the uploaded general email documents.
     *
     * @return the uploaded documents, or an empty list if none exist
     */
    @JsonIgnore
    public List<CaseDocument> getUploadedDocuments() {
        return emptyIfNull(generalEmailUploadedDocuments)
            .stream()
            .filter(Objects::nonNull)
            .map(DocumentCollectionItem::getValue)
            .filter(Objects::nonNull)
            .toList();
    }
}
