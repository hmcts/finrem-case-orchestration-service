package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class AmendedConsentOrder {
    @JsonProperty("amendedConsentOrder")
    private CaseDocument amendedConsentOrder;
    @JsonProperty("amendedConsentOrderDate")
    private LocalDate amendedConsentOrderDate;
}
