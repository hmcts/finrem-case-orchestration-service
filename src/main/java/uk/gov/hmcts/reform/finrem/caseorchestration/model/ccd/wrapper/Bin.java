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

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bin {

    @JsonProperty("bin_fileUrls")
    private DynamicList fileUrlsToBeDeleted;

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

    public void clearBin() {
        this.fileUrlsToBeDeleted = null;
    }
}
