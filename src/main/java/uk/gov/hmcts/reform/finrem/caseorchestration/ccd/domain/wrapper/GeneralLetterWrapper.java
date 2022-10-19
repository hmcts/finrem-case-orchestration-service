package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.GeneralLetterCollection;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralLetterWrapper {
    private GeneralLetterAddressToType generalLetterAddressTo;
    private String generalLetterRecipient;
    private Address generalLetterRecipientAddress;
    private String generalLetterCreatedBy;
    private String generalLetterBody;
    private Document generalLetterPreview;
    private List<GeneralLetterCollection> generalLetterCollection;
}
