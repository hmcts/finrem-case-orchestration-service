package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

/**
 * Provides a contract for retrieving a document's unique identifier.
 * Implementing classes should provide their own logic to return the document ID.
 */
public interface DocumentIdProvider {

    /**
     * Retrieves the unique identifier of the document.
     *
     * @return the document ID as a {@link String}.
     */
    String getDocumentId();
}
