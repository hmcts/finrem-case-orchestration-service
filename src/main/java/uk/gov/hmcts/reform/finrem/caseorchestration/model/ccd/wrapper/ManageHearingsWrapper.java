package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingVacatedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.VacatedOrAdjournedHearingTabCollectionItem;

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

    // Working data representations
    private ManageHearingsAction manageHearingsActionSelection;
    private UUID workingHearingId;
    private WorkingHearing workingHearing;
    private WorkingVacatedHearing workingVacatedHearing;
    private UUID workingVacatedHearingId;
    private YesOrNo isRelistSelected;
    private YesOrNo wasRelistSelected;
    private YesOrNo isAddHearingChosen;
    private YesOrNo isFinalOrder;
    private YesOrNo shouldSendVacateOrAdjNotice;

    // Hearing data Repositories
    private List<ManageHearingsCollectionItem> hearings;
    private List<VacatedOrAdjournedHearingsCollectionItem> vacatedOrAdjournedHearings;
    private List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection;

    // TabItem representations maintaining confidentiality for parties
    private List<HearingTabCollectionItem> hearingTabItems;

    private List<HearingTabCollectionItem> applicantHearingTabItems;
    private List<HearingTabCollectionItem> respondentHearingTabItems;
    private List<HearingTabCollectionItem> int1HearingTabItems;
    private List<HearingTabCollectionItem> int2HearingTabItems;
    private List<HearingTabCollectionItem> int3HearingTabItems;
    private List<HearingTabCollectionItem> int4HearingTabItems;

    // Vacated Or Adjourned Hearing Tab Items
    private List<VacatedOrAdjournedHearingTabCollectionItem> vacatedOrAdjournedHearingTabItems;

    private List<VacatedOrAdjournedHearingTabCollectionItem> applicantVacOrAdjHearingTabItems;
    private List<VacatedOrAdjournedHearingTabCollectionItem> respondentVacOrAdjHearingTabItems;
    private List<VacatedOrAdjournedHearingTabCollectionItem> int1VacOrAdjHearingTabItems;
    private List<VacatedOrAdjournedHearingTabCollectionItem> int2VacOrAdjHearingTabItems;
    private List<VacatedOrAdjournedHearingTabCollectionItem> int3VacOrAdjHearingTabItems;
    private List<VacatedOrAdjournedHearingTabCollectionItem> int4VacOrAdjHearingTabItems;

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

    /**
     * Retrieves a {@link VacatedOrAdjournedHearingsCollectionItem} from the hearings list by its UUID.
     *
     * <p>
     * If the list is {@code null} or no item matches the provided ID, this method returns {@code null}.
     * </p>
     *
     * @param requiredId the UUID of the hearing item to retrieve
     * @return the matching {@link VacatedOrAdjournedHearingsCollectionItem}, or {@code null} if not found
     */
    public VacatedOrAdjournedHearingsCollectionItem getVacatedOrAdjournedHearingsCollectionItemById(UUID requiredId) {
        return Optional.ofNullable(vacatedOrAdjournedHearings)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(Objects::nonNull)
            .filter(item -> requiredId != null && requiredId.equals(item.getId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns the UUID for workingVacatedHearing.getChooseHearings().getValue().getCode().
     * workingVacatedHearingId for working vacated hearings, workingVacatedHearingId for new relisted hearings
     * @return UUID which is the unique id for the working vacated hearing (corresponds to an actual vacated hearing).
     */
    public UUID getWorkingVacatedHearingId() {
        if (workingVacatedHearingId == null) {
            workingVacatedHearingId = Optional.ofNullable(getWorkingVacatedHearing())
                .map(WorkingVacatedHearing::getChooseHearings)
                .map(DynamicList::getValue)
                .map(DynamicListElement::getCode)
                .map(UUID::fromString)
                .orElse(null);
        }
        return workingVacatedHearingId;
    }

    /**
     * Retrieves a list of working hearing-related documents associated with the current hearing ID.
     *
     * The method filters the hearingDocumentsCollection to include only those documents that are
     * linked to the current working hearing ID. If either the working hearing ID or the
     * hearingDocumentsCollection is null, an empty list is returned.
     *
     * @return a list of {@link CaseDocument} objects corresponding to the working hearing ID,
     *         or an empty list if no matching documents are found or if the input data is null.
     */
    @JsonIgnore
    public List<CaseDocument> getAssociatedWorkingHearingDocuments() {
        UUID hearingId = getWorkingHearingId();
        if (hearingId == null || hearingDocumentsCollection == null) {
            return Collections.emptyList();
        }
        return hearingDocumentsCollection.stream()
            .map(ManageHearingDocumentsCollectionItem::getValue)
            .filter(doc -> hearingId.equals(doc.getHearingId()))
            .map(ManageHearingDocument::getHearingDocument)
            .toList();
    }
}
