package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingTabItem {
    private String tabHearingType;
    private String tabCourtSelection;
    private String tabAttendance;
    private String tabDateTime;
    private String tabTimeEstimate;
    private String tabConfidentialParties;
    private String tabAdditionalInformation;
    private List<DocumentCollectionItem> tabHearingDocuments;
    private YesOrNo tabWasMigrated;
}
