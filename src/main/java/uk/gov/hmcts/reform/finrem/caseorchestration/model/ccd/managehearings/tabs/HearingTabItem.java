package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingTabItem {
    private String hearingType;
    private String courtSelection;
    private String hearingAttendance;
    private String hearingDateTime;
    private String timeEstimate;
    private String hearingConfidentialParties;
    private String additionalHearingInformation;
    private List<CaseDocument> hearingDocuments;
}
