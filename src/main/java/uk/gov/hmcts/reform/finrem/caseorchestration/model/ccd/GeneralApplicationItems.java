package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
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
    @JsonProperty("gaSupportDocuments")
    private List<GeneralApplicationSupportingDocumentData> gaSupportDocuments;
    @JsonProperty("generalApplicationStatus")
    private String generalApplicationStatus;
    @JsonProperty("generalApplicationOutcomeOther")
    private String generalApplicationOutcomeOther;
    @JsonProperty("generalApplicationDirectionsDocument")
    private CaseDocument generalApplicationDirectionsDocument;
}
