package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bin {

    @JsonProperty("bin_fileUrls")
    private DynamicList fileUrlsToBeDeleted;

    /**
     * Adds the given case document URL to the bin of files scheduled for deletion.
     *
     * <p>If the bin has not yet been initialised, a new {@link DynamicList} is created
     * with an empty list of items before the document URL is added.</p>
     *
     * <p>The document URL is stored as the {@code code} value of a
     * {@link DynamicListElement}.</p>
     *
     * @param caseDocument the case document whose document URL should be added to the deletion bin
     */
    public void binCaseDocument(CaseDocument caseDocument) {
        if (this.fileUrlsToBeDeleted == null) {
            this.fileUrlsToBeDeleted = DynamicList.builder()
                .listItems(new ArrayList<>())
                .build();
        }
        this.fileUrlsToBeDeleted.getListItems().add(
            DynamicListElement.builder().code(caseDocument.getDocumentUrl()).build()
        );
    }

    /**
     * Identifies documents that existed previously but are no longer present
     * in the current collection, and adds them to the deletion bin.
     *
     * <p>Both streams are filtered to ignore {@code null} values before comparison.</p>
     *
     * <p>A document is considered deleted when its document URL exists in the
     * {@code previousDocuments} stream but not in the {@code currentDocuments} stream.</p>
     *
     * @param previousDocuments the stream of documents that existed previously
     * @param currentDocuments the stream of documents that currently exist
     */
    public void binDeletedCaseDocument(Stream<CaseDocument> previousDocuments,
                                       Stream<CaseDocument> currentDocuments) {
        Set<String> currentDocumentUrls = currentDocuments
            .filter(Objects::nonNull)
            .map(CaseDocument::getDocumentUrl)
            .collect(Collectors.toSet());

        previousDocuments
            .filter(Objects::nonNull)
            .filter(document -> !currentDocumentUrls.contains(document.getDocumentUrl()))
            .forEach(this::binCaseDocument);
    }

    /**
     * Clears all document URLs currently stored in the deletion bin.
     *
     * <p>After this method is invoked, no files remain marked for deletion.</p>
     */
    public void clearBin() {
        this.fileUrlsToBeDeleted = null;
    }
}
