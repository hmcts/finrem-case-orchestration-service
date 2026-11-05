package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

/**
 * Holds the cover letters generated in about-to-submit for the consented Send Order events,
 * so they can be sent to the parties in the submitted callback.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentSendOrderWrapper {

    private CaseDocument consentOrderApprovedCoverLetterApp;
    private CaseDocument consentOrderApprovedCoverLetterResp;
    private CaseDocument consentOrderNotApprovedCoverLetterApp;
    private CaseDocument consentOrderNotApprovedCoverLetterResp;
}
