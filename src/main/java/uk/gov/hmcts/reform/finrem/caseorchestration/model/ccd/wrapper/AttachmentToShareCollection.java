package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a collection wrapper for an attachment to be shared within the case orchestration process.
 * This class encapsulates an instance of {@link AttachmentToShare}, providing a structured way
 * to handle document attachments.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentToShareCollection {

    /**
     * The attachment details encapsulated within this collection.
     */
    private AttachmentToShare value;
}
