package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

@Component
@AllArgsConstructor
public class PostalHearingDocumentsProvider {

    private final List<PostalHearingDocumentSupplier> suppliers;

    /**
     * Retrieves all hearing documents that need to be posted for the current working hearing.
     * Note: FDR Hearings should only reach this point for Express cases.
     * @param finremCaseDetails the case details containing the hearing documents
     * @return a {@link CaseDocument}
     */
    public List<CaseDocument> getHearingDocumentsToPost(FinremCaseDetails finremCaseDetails) {

        return suppliers.stream()
            .map(supplier -> supplier.get(finremCaseDetails))
            .flatMap(List::stream)
            .toList();
    }
}
