package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovedOrder {
    @JsonProperty("orderLetter")
    private CaseDocument orderLetter;
    @JsonProperty("consentOrder")
    private CaseDocument consentOrder;
    @JsonProperty("pensionDocuments")
    private List<PensionCollectionData> pensionDocuments;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("consentOrderApprovedNotificationLetter")
    private CaseDocument consentOrderApprovedNotificationLetter;
}
