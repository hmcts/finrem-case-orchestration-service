package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleApprovedOrder {
    @JsonProperty("orderLetter")
    private CaseDocument orderLetter;

    @JsonProperty("consentOrder")
    private CaseDocument consentOrder;

    @JsonProperty("pensionDocuments")
    private List<PensionTypeCollection> pensionDocuments;

    @JsonProperty("additionalConsentDocuments")
    private List<DocumentCollection> additionalConsentDocuments;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime orderReceivedAt;
}
