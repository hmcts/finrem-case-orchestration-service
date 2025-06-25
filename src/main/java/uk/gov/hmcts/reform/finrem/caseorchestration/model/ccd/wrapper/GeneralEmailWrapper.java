package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralEmailWrapper implements HasCaseDocument {
    private String generalEmailRecipient;
    private String generalEmailCreatedBy;
    private String generalEmailBody;
    private LocalDateTime generalEmailDateSent;
    private CaseDocument generalEmailUploadedDocument;
    private List<GeneralEmailCollection> generalEmailCollection;

    /**
     * Clears working data, but leaves generalEmailCollection untouched.
     * The data set to null below is working data, used to capture User input from an EXUI event.
     * The generalEmailCollection is preserved as a record of emails sent.
     */
    public void setGeneralEmailValuesToNull() {
        this.setGeneralEmailRecipient(null);
        this.setGeneralEmailCreatedBy(null);
        this.setGeneralEmailUploadedDocument(null);
        this.setGeneralEmailBody(null);
        this.setGeneralEmailDateSent(null);
    }
}
