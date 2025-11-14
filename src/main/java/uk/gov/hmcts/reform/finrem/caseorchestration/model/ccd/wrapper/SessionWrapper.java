package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionWrapper {

    @TemporaryField
    private YesOrNo loginAsApplicantSolicitor;

    @TemporaryField
    private YesOrNo loginAsRespondentSolicitor;

    public boolean isLoginAsApplicantSolicitor() {
        return YesOrNo.isYes(loginAsApplicantSolicitor);
    }

    public boolean isLoginAsRespondentSolicitor() {
        return YesOrNo.isYes(loginAsRespondentSolicitor);
    }
}
