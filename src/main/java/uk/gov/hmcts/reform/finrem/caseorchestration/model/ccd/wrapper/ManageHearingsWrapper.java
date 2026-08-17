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

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCuPlus1RolesOsjhrjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCPlus1RolesTqxnytAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManageHearingsWrapper {

    // Working data representations
    @CCD(
            label = "What would you like to do?",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private ManageHearingsAction manageHearingsActionSelection;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private UUID workingHearingId;
    @CCD(
            label = "Hearing",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private WorkingHearing workingHearing;
    @CCD(
            label = "Adjourn or Vacate hearing",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private WorkingVacatedHearing workingVacatedHearing;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private UUID workingVacatedHearingId;
    @CCD(
            label = "Will you be relisting the hearing and adding a new date now?",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private YesOrNo isRelistSelected;
    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceSystemupdateCrudAccess.class})
    private YesOrNo wasRelistSelected;
    @CCD(
            label = "Do you want to add a hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private YesOrNo isAddHearingChosen;
    @CCD(
            label = "Is this the final order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private YesOrNo isFinalOrder;
    @CCD(
            label = "Do you want to send notices?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private YesOrNo shouldSendVacateOrAdjNotice;

    // Hearing data Repositories
    @CCD(
            label = "Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_hearing",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private List<ManageHearingsCollectionItem> hearings;
    @CCD(
            label = "Adjourned or Vacated Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_vacateOrAdjournHearing",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    private List<VacatedOrAdjournedHearingsCollectionItem> vacatedOrAdjournedHearings;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageHearingDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesIwxsitAccess.class}
    )
    private List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection;

    // TabItem representations maintaining confidentiality for parties
    @CCD(
            label = "Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageHearingTabItem",
            access = {CaseworkerDivorceFinancialremedyCourtadminCuPlus1RolesOsjhrjAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private List<HearingTabCollectionItem> hearingTabItems;

    @CCD(
            label = "Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageHearingTabItem",
            access = {CaseworkerDivorceFinancialremedyCourtadminCPlus1RolesTqxnytAccess.class}
    )
    private List<HearingTabCollectionItem> applicantHearingTabItems;
    @CCD(
            label = "Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageHearingTabItem",
            access = {CaseworkerDivorceFinancialremedyCourtadminCPlus1RolesTqxnytAccess.class}
    )
    private List<HearingTabCollectionItem> respondentHearingTabItems;
    @CCD(
            label = "Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageHearingTabItem",
            access = {CaseworkerDivorceFinancialremedyCourtadminCPlus1RolesTqxnytAccess.class}
    )
    private List<HearingTabCollectionItem> int1HearingTabItems;
    @CCD(
            label = "Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageHearingTabItem",
            access = {CaseworkerDivorceFinancialremedyCourtadminCPlus1RolesTqxnytAccess.class}
    )
    private List<HearingTabCollectionItem> int2HearingTabItems;
    @CCD(
            label = "Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageHearingTabItem",
            access = {CaseworkerDivorceFinancialremedyCourtadminCPlus1RolesTqxnytAccess.class}
    )
    private List<HearingTabCollectionItem> int3HearingTabItems;
    @CCD(
            label = "Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_manageHearingTabItem",
            access = {CaseworkerDivorceFinancialremedyCourtadminCPlus1RolesTqxnytAccess.class}
    )
    private List<HearingTabCollectionItem> int4HearingTabItems;

    // Vacated Or Adjourned Hearing Tab Items
    @CCD(
            label = "Adjourned or Vacated Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_vacatedOrAdjournedHearingTabItems",
            access = {CaseworkerDivorceFinancialremedyCourtadminCuPlus1RolesOsjhrjAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private List<VacatedOrAdjournedHearingTabCollectionItem> vacatedOrAdjournedHearingTabItems;

    @CCD(
            label = "Adjourned or Vacated Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_vacatedOrAdjournedHearingTabItems",
            access = {CaseworkerDivorceFinancialremedyCourtadminCAccess.class}
    )
    private List<VacatedOrAdjournedHearingTabCollectionItem> applicantVacOrAdjHearingTabItems;
    @CCD(
            label = "Adjourned or Vacated Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_vacatedOrAdjournedHearingTabItems",
            access = {CaseworkerDivorceFinancialremedyCourtadminCAccess.class}
    )
    private List<VacatedOrAdjournedHearingTabCollectionItem> respondentVacOrAdjHearingTabItems;
    @CCD(
            label = "Adjourned or Vacated Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_vacatedOrAdjournedHearingTabItems",
            access = {CaseworkerDivorceFinancialremedyCourtadminCAccess.class}
    )
    private List<VacatedOrAdjournedHearingTabCollectionItem> int1VacOrAdjHearingTabItems;
    @CCD(
            label = "Adjourned or Vacated Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_vacatedOrAdjournedHearingTabItems",
            access = {CaseworkerDivorceFinancialremedyCourtadminCAccess.class}
    )
    private List<VacatedOrAdjournedHearingTabCollectionItem> int2VacOrAdjHearingTabItems;
    @CCD(
            label = "Adjourned or Vacated Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_vacatedOrAdjournedHearingTabItems",
            access = {CaseworkerDivorceFinancialremedyCourtadminCAccess.class}
    )
    private List<VacatedOrAdjournedHearingTabCollectionItem> int3VacOrAdjHearingTabItems;
    @CCD(
            label = "Adjourned or Vacated Hearings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_vacatedOrAdjournedHearingTabItems",
            access = {CaseworkerDivorceFinancialremedyCourtadminCAccess.class}
    )
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

    /**
     * Retrieves a list of hearing-related documents associated with the specified hearing ID.
     * The method filters the hearingDocumentsCollection to include only those documents
     * linked to the provided hearing ID. If either the hearing ID or the hearingDocumentsCollection
     * is null, an empty list is returned.
     *
     * @param hearingId the UUID of the hearing whose associated documents are to be retrieved
     * @return a list of {@link CaseDocument} objects corresponding to the given hearing ID,
     *         or an empty list if no matching documents are found or if the input data is null
     */
    @JsonIgnore
    public List<CaseDocument> getAssociatedHearingDocuments(UUID hearingId) {
        if (hearingId == null || hearingDocumentsCollection == null) {
            return Collections.emptyList();
        }
        return hearingDocumentsCollection.stream()
            .map(ManageHearingDocumentsCollectionItem::getValue)
            .filter(doc -> hearingId.equals(doc.getHearingId()))
            .map(ManageHearingDocument::getHearingDocument)
            .toList();
    }

    /*
     * Returns true if the hearings collection is empty or null
     * Does not consider vacatedOrAdjournedHearings.
     *
     * @param caseData The case data.
     * @return true if the hearings collection is empty or null, false otherwise.
     */
    @JsonIgnore
    public boolean hasNoHearings() {
        return isEmpty(getHearings());
    }
}
