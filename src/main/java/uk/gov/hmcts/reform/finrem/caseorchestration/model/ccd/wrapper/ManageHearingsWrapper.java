package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManageHearingsWrapper {

    private ManageHearingsAction manageHearingsActionSelection;
    private Hearing workingHearing;
    private List<ManageHearingsCollectionItem> hearings;
    private List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection;
    private UUID workingHearingId;
    private List<HearingTabCollectionItem> hearingTabItems;
    private List<HearingTabCollectionItem> applicantHearingTabItems;
    private List<HearingTabCollectionItem> respondentHearingTabItems;
    private List<HearingTabCollectionItem> int1HearingTabItems;
    private List<HearingTabCollectionItem> int2HearingTabItems;
    private List<HearingTabCollectionItem> int3HearingTabItems;
    private List<HearingTabCollectionItem> int4HearingTabItems;
}
