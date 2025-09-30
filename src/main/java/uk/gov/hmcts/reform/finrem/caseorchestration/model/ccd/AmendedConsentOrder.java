package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils;

import java.time.LocalDate;
import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class AmendedConsentOrder implements HasCaseDocument, HasUploadingDocuments {
    @JsonProperty("amendedConsentOrder")
    private CaseDocument amendedConsentOrder;
    @JsonProperty("amendedConsentOrderDate")
    private LocalDate amendedConsentOrderDate;

    @Override
    @JsonIgnore
    public List<CaseDocument> getUploadingDocuments() {
        return ListUtils.safeListWithoutNulls(amendedConsentOrder);
    }
}
