package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManageHearingsWrapper {
    private YesOrNo isAddHearingChosen;
    private YesOrNo isFinalOrder;
    private ManageHearingsAction manageHearingsActionSelection;
    private WorkingHearing workingHearing;
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

    /**
     * Retrieves a {@link ManageHearingsCollectionItem} from the hearings list by its UUID.
     *
     * <p>
     * If the hearings list is {@code null} or no item matches the provided ID, this method returns {@code null}.
     * </p>
     *
     * @param requiredId the UUID of the hearing item to retrieve
     * @return the matching {@link ManageHearingsCollectionItem}, or {@code null} if not found
     */
    public ManageHearingsCollectionItem getManageHearingsCollectionItemById(UUID requiredId) {
        return Optional.ofNullable(hearings)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(Objects::nonNull)
            .filter(item -> requiredId != null && requiredId.equals(item.getId()))
            .findFirst()
            .orElse(null);
    }
}
