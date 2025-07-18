package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

/**
 * Represents a cover letter that can be shared within the case orchestration process.
 * This class provides details such as the document ID, cover letter name.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoverLetterToShare implements DocumentIdProvider {

    /**
     * The unique identifier of the document.
     */
    private String documentId;

    /**
     * The name of the coverLetter.
     */
    private String coverLetterName;

    private CaseDocument coverLetterDocument;

}
