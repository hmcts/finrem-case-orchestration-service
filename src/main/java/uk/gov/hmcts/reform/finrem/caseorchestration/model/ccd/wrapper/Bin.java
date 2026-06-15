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
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bin {

    @Deprecated(forRemoval = true)
    @JsonProperty("bin_fileUrls")
    private DynamicList binFileUrls;

    @JsonProperty("bin_fileUrlsCollection")
    private List<BinFileUrlsCollection> fileUrlsToBeDeleted;

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
        Optional.ofNullable(caseDocument)
            .map(CaseDocument::getDocumentUrl)
            .ifPresent(documentUrl -> {
                if (fileUrlsToBeDeleted == null) {
                    fileUrlsToBeDeleted = new ArrayList<>();
                }
                fileUrlsToBeDeleted.add(toBinFileUrlsCollection(caseDocument.getDocumentUrl()));
            });
    }

    /**
     * Clears all document URLs currently stored in the deletion bin.
     *
     * <p>After this method is invoked, no files remain marked for deletion.</p>
     */
    public void clearBin() {
        this.fileUrlsToBeDeleted = null;
    }

    private BinFileUrlsCollection toBinFileUrlsCollection(String documentUrl) {
        return BinFileUrlsCollection.builder()
                .value(BinFileUrls.builder()
                        .binFileUrl(documentUrl)
                        .build())
                .build();
    }
}
