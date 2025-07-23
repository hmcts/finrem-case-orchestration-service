package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCollectionItem implements HasCaseDocument {
    private UUID id;
    private CaseDocument value;

    /**
     * Converts a {@link CaseDocument} into a {@link DocumentCollectionItem}.
     *
     * @param caseDocument the case document to convert; may be {@code null}
     * @return a {@link DocumentCollectionItem} wrapping the given case document,
     *         or {@code null} if the input is {@code null}
     */
    public static DocumentCollectionItem fromCaseDocument(CaseDocument caseDocument) {
        return caseDocument == null ? null : DocumentCollectionItem.builder().value(caseDocument).build();
    }
}
