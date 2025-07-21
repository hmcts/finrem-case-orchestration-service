package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Yes;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * Represents an order document that can be shared within the case orchestration process.
 * This class provides details such as the document ID, name, and related sharing preferences.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderToShare implements DocumentIdProvider {

    private String documentId;

    private String documentName;

    private YesOrNo documentToShare;

    private YesOrNo hasSupportingDocuments;
    
    private List<Yes> includeSupportingDocument;

    private List<AttachmentToShareCollection> attachmentsToShare;

    private CoverLetterToShare coverLetterToShare;

    /**
     * Determines if the order has attachments.
     *
     * @return {@link YesOrNo#YES} if attachments exist, otherwise {@link YesOrNo#NO}.
     */
    @JsonIgnore
    public YesOrNo getHasAttachment() {
        return YesOrNo.forValue(ofNullable(attachmentsToShare).isEmpty());
    }

    /**
     * Determines if the order has a cover letter.
     *
     * @return {@link YesOrNo#YES} if attachments exist, otherwise {@link YesOrNo#NO}.
     */
    @JsonIgnore
    public YesOrNo getHasCoverLetter() {
        return YesOrNo.forValue(ofNullable(coverLetterToShare).isEmpty());
    }

    /**
     * Checks if supporting documents should be included.
     *
     * @return {@code true} if supporting documents are included, otherwise {@code false}.
     */
    public boolean shouldIncludeSupportingDocuments() {
        return CollectionUtils.isNotEmpty(includeSupportingDocument);
    }

}
