package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;

@Data
@Builder
public class CaseDocumentsHolder {

    List<CaseDocument> caseDocuments;
    List<BulkPrintDocument> bulkPrintDocuments;
}
