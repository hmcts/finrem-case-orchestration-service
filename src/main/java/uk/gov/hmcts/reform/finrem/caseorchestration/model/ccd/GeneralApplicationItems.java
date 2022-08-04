package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralApplicationItems {
    @JsonProperty("generalApplicationDocument")
    private CaseDocument generalApplicationDocument;
    @JsonProperty("generalApplicationCreatedBy")
    private String generalApplicationCreatedBy;
    @JsonProperty("generalApplicationDraftOrder")
    private CaseDocument generalApplicationDraftOrder;
    @JsonProperty("generalApplicationReceivedFrom")
    private String generalApplicationReceivedFrom;
    @JsonProperty("generalApplicationTimeEstimate")
    private String generalApplicationTimeEstimate;
    @JsonProperty("generalApplicationHearingRequired")
    private String generalApplicationHearingRequired;
    @JsonProperty("generalApplicationSpecialMeasures")
    private String generalApplicationSpecialMeasures;
    @JsonProperty("generalApplicationCreatedDate")
    private LocalDate generalApplicationCreatedDate;
}
