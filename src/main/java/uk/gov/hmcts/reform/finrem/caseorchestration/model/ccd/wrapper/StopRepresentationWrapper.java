package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopRepresentationWrapper {

    @TemporaryField
    private YesOrNo stopRepClientConsent;

    @TemporaryField
    private YesOrNo stopRepJudicialApproval;

    @TemporaryField
    private Address clientAddressForService;

    @TemporaryField
    private YesOrNo clientAddressForServiceConfidential;

    @TemporaryField
    private String clientAddressForServiceConfidentialLabel;
}
