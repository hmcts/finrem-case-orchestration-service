package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import java.util.List;

/**
 * Represents an entity that contains a list of case document attachments.
 * This interface provides a method to retrieve the associated attachments.
 */
public interface WithAttachments {

    /**
     * Retrieves a list of case document attachments.
     *
     * @return a list of {@link CaseDocumentCollection} representing the attachments.
     */
    List<CaseDocumentCollection> getAttachments();
}
