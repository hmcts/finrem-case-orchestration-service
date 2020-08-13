package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;


@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@AllArgsConstructor
public class ContestedConsentOrder {
    @JsonProperty("consentOrder")
    private CaseDocument consentOrder;
}