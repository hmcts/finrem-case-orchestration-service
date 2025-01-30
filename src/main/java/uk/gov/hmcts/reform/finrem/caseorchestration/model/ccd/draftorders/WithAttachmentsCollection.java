package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

/**
 * Represents an entity that contains an attachments collection.
 * This interface provides a method to retrieve the attachment-related value.
 */
public interface WithAttachmentsCollection {

    /**
     * Retrieves the value containing attachment details.
     *
     * @return an instance of {@link WithAttachments} containing attachment data.
     */
    WithAttachments getValue();
}
