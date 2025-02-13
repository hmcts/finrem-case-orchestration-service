package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;

import java.util.List;

/**
 * Represents an entity that contains a list of uploading documents.
 *
 * <p>
 * This interface is used in {@link NewUploadedDocumentsService} to identify newly uploaded documents
 * by comparing the current and previous case data. It is also utilised in {@link DocumentWarningsHelper}
 * to process these documents and generate warnings.
 */
public interface HasUploadingDocuments {

    List<CaseDocument> getUploadingDocuments();
}
