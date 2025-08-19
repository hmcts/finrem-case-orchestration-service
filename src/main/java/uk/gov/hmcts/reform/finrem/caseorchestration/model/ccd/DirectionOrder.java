package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.WithAttachments;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DirectionOrder implements HasCaseDocument, WithAttachments, HasUploadingDocuments {
    @JsonProperty("uploadDraftDocument")
    CaseDocument uploadDraftDocument;
    @JsonProperty("orderDateTime")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    LocalDateTime orderDateTime;
    @JsonProperty("isOrderStamped")
    YesOrNo isOrderStamped;
    @JsonProperty("originalDocument")
    CaseDocument originalDocument;
    @JsonProperty("additionalDocuments")
    private List<DocumentCollectionItem> additionalDocuments;

    @Override
    @JsonIgnore
    public List<DocumentCollectionItem> getAttachments() {
        return additionalDocuments;
    }

    @Override
    @JsonIgnore
    public List<CaseDocument> getUploadingDocuments() {
        return uploadDraftDocument != null ? List.of(uploadDraftDocument) : List.of();
    }
}
