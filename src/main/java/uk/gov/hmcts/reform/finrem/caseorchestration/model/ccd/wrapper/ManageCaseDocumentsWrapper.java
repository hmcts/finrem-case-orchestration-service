package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManageCaseDocumentsWrapper implements HasCaseDocument {

    private ManageCaseDocumentsAction manageCaseDocumentsActionSelection;

    // It was used for capturing user input in the old event.
    // It’s kept to maintain compatibility with the existing document handler logic.
    private List<UploadCaseDocumentCollection> manageCaseDocumentCollection;

    // it's used for capturing user's input in FR_newManageCaseDocuments event.
    private List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection;
}
