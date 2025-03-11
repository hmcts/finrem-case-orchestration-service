package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

/**
 * Represents an attachment that can be shared within the case orchestration process.
 * This class provides details such as the document ID, attachment name, and whether
 * the document is marked for sharing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentToShare implements DocumentIdProvider {

    /**
     * The unique identifier of the document.
     */
    private String documentId;

    /**
     * The name of the attachment.
     */
    private String attachmentName;

    /**
     * Indicates whether the document is to be shared.
     * Possible values: {@link YesOrNo#YES} or {@link YesOrNo#NO}.
     */
    private YesOrNo documentToShare;

    private CaseDocument attachment;

}
