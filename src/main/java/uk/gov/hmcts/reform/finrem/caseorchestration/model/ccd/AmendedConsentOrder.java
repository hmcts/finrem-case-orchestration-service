package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AmendedConsentOrder {
    @JsonProperty("amendedConsentOrder")
    private CaseDocument amendedConsentOrder;
    @JsonProperty("amendedConsentOrderDate")
    private Date amendedConsentOrderDate;
}
