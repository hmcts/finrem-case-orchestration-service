package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralEmailWrapper implements HasCaseDocument {
    @TemporaryField
    private String generalEmailRecipient;
    @TemporaryField
    private String generalEmailCreatedBy;
    @TemporaryField
    private String generalEmailBody;
    @TemporaryField
    private CaseDocument generalEmailUploadedDocument;

    // It stores the emails sent
    private List<GeneralEmailCollection> generalEmailCollection;
}
