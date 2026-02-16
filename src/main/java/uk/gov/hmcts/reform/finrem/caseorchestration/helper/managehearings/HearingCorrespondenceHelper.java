package uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.FDA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.FDR;

@RequiredArgsConstructor
@Slf4j
@Component
public class HearingCorrespondenceHelper {

    private final PaperNotificationService paperNotificationService;
    private final ExpressCaseService expressCaseService;

    /**
     * Retrieves the {@link Hearing} from the wrapper param using the UUID param.
     *
     * <p>If the hearings list is {@code null}, or if no hearing matches the working hearing ID,
     * this method throws an {@link IllegalStateException} </p>
     *
     * <p>A working hearing refers to the {@link Hearing} that a user is actively creating or modifying in EXUI.</p>
     *
     * @param wrapper the ManageHearingsWrapper instance containing hearings.
     * @param workingHearingId UUID for the working hearing OK.
     * @return the {@link Hearing} associated with the current working hearing ID
     * @throws IllegalStateException if the hearings list is missing, or no matching hearing is found
     */
    public Hearing getActiveHearingInContext(ManageHearingsWrapper wrapper, UUID workingHearingId) {

        List<ManageHearingsCollectionItem> hearings = wrapper.getHearings();

        if (hearings == null) {
            throw new IllegalStateException(
                "No hearings available to search for. Working hearing ID is: " + workingHearingId
            );
        }

        return wrapper.getHearings().stream()
            .filter(h -> workingHearingId.equals(h.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Hearing not found for the given ID: " + workingHearingId))
            .getValue();
    }

    public VacateOrAdjournedHearing getVacateOrAdjournedHearingInContext(ManageHearingsWrapper wrapper, UUID workingVacatedHearingId) {

        List<VacatedOrAdjournedHearingsCollectionItem> hearings = wrapper.getVacatedOrAdjournedHearings();

        if (hearings == null) {
            throw new IllegalStateException(
                "No vacated or adjourned hearings available to search for. Working hearing ID is: " + workingVacatedHearingId
            );
        }

        return Optional.ofNullable(wrapper.getVacatedOrAdjournedHearingsCollectionItemById(workingVacatedHearingId))
            .map(VacatedOrAdjournedHearingsCollectionItem::getValue)
            .orElseThrow(() -> new IllegalStateException("Vacated hearing not found for the given ID: " + workingVacatedHearingId));
    }

    /**
     * Retrieves the {@link HearingTabItem} currently in context based on the working hearing ID from the case data.
     *
     * <p>This method accesses the {@link ManageHearingsWrapper} from the provided {@link FinremCaseData},
     * and uses the working hearing ID to locate the matching {@link HearingTabItem} in the list of hearing tab items.</p>
     *
     * <p>If no item matches the working hearing ID,
     * this method throws an {@link IllegalStateException}.</p>
     *
     * <p>A working hearing refers to the {@link HearingTabItem} a user is actively creating or modifying in the UI.</p>
     *
     * @param finremCaseData the case data containing the hearing tab items and context
     * @return the {@link HearingTabItem} associated with the current working hearing ID
     * @throws IllegalStateException if the hearing tab items list is missing, or no matching item is found
     */
    public HearingTabItem getHearingInContextFromTab(FinremCaseData finremCaseData) {
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();
        UUID hearingId = manageHearingsWrapper.getWorkingHearingId();

        return manageHearingsWrapper.getHearingTabItems().stream()
            .filter(h -> hearingId.equals(h.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Hearing Tab Item not found for the given ID: " + hearingId))
            .getValue();
    }

    /**
     * Wraps {@link PaperNotificationService} logic for readability.
     *
     * @return true if the applicant should receive hearing documents by post.
     */
    public boolean shouldPostToApplicant(FinremCaseDetails finremCaseDetails) {
        return paperNotificationService.shouldPrintForApplicantDisregardApplicationType(finremCaseDetails);
    }

    /**
     * Wraps {@link PaperNotificationService} logic for readability.
     *
     * @return true if the respondent should receive hearing documents by post.
     */
    public boolean shouldPostToRespondent(FinremCaseDetails finremCaseDetails) {
        return paperNotificationService.shouldPrintForRespondent(finremCaseDetails);
    }

    /**
     * Get the vacate hearing notice document, or return null if not found.
     *
     * @param caseData case details containing the hearing documents
     * @return a {@link CaseDocument}
     */
    public CaseDocument getVacateHearingNotice(FinremCaseData caseData) {
        return getByWorkingVacatedHearingAndDocumentType(caseData, CaseDocumentType.VACATE_HEARING_NOTICE);
    }

    /**
     * Retrieves the action selection, e.g. ADD_HEARING, from the Manage Hearings Wrapper in the case details.
     *
     * @param caseData the case details containing the Manage Hearings Wrapper
     * @return the ManageHearingsAction or null if not present
     */
    public ManageHearingsAction getManageHearingsAction(FinremCaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(FinremCaseData::getManageHearingsWrapper)
            .map(ManageHearingsWrapper::getManageHearingsActionSelection)
            .orElse(null);
    }

    /**
     * Returns true is a hearing was vacated and relisted.
     *
     * @param caseData queried to see if the vacate action was chosen and if the hearing was relisted.
     * @return if the hearing was vacated and relisted
     */
    public boolean isVacatedAndRelistedHearing(FinremCaseData caseData) {
        ManageHearingsAction actionSelection = getManageHearingsAction(caseData);
        boolean hearingRelisted = YesOrNo.YES.equals(
            caseData.getManageHearingsWrapper().getWasRelistSelected());
        return isAdjournOrVacateHearingAction(actionSelection) && hearingRelisted;
    }

    /**
     * Determines if the Mini Form A document is required based on the hearing type and case type,
     * and retrieves it if applicable.
     * For FDA hearings, the Mini Form A is always required. For FDR hearings, it is conditionally
     * required if the case is an express case.
     *
     * @param caseData the case data containing relevant details about the case
     * @param hearing the hearing information used to determine if the Mini Form A is required
     * @return an {@link Optional} containing the Mini Form A document if it is required,
     *         or an empty {@link Optional} if not
     */
    public Optional<CaseDocument> getMiniFormAIfRequired(FinremCaseData caseData, Hearing hearing) {
        if (FDA.equals(hearing.getHearingType())
            || (FDR.equals(hearing.getHearingType()) && expressCaseService.isExpressCase(caseData))) {
            return Optional.ofNullable(caseData.getMiniFormA());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Determines if the action selection is to vacate a hearing.
     *
     * @param actionSelection the action selection to check
     * @return true if the action selection is ADJOURN_OR_VACATE_HEARING, false otherwise
     */
    private boolean isAdjournOrVacateHearingAction(ManageHearingsAction actionSelection) {
        return ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.equals(actionSelection);
    }

    /**
     * Gets the most recent Vacated hearing document with the passed CaseDocumentType argument.
     * If no notice is found, returns an empty list.
     *
     * @param caseData     the case details containing the hearing documents.
     * @param documentType a {@link CaseDocumentType} identifying the type of hearing document.
     * @return a {@link CaseDocument}
     */
    private CaseDocument getByWorkingVacatedHearingAndDocumentType(FinremCaseData caseData,
                                                                   CaseDocumentType documentType) {
        ManageHearingsWrapper wrapper = caseData.getManageHearingsWrapper();
        UUID hearingId = wrapper.getWorkingVacatedHearingId();
        return getCaseDocumentByTypeAndHearingUuid(documentType, wrapper, hearingId);
    }

    /**
     * Retrieves a case document filtered on the UUID of the hearing and document type.
     *
     * @param wrapper      the ManageHearings wrapper holding the docs
     * @param documentType a {@link CaseDocumentType} identifying the type of hearing document.
     * @return a {@link CaseDocument}
     */
    public CaseDocument getCaseDocumentByTypeAndHearingUuid(CaseDocumentType documentType, ManageHearingsWrapper wrapper, UUID hearingId) {
        return wrapper.getHearingDocumentsCollection().stream()
            .map(ManageHearingDocumentsCollectionItem::getValue)
            .filter(Objects::nonNull)
            .filter(doc -> Objects.equals(hearingId, doc.getHearingId()))
            .filter(doc -> Objects.equals(documentType, doc.getHearingCaseDocumentType()))
            .map(ManageHearingDocument::getHearingDocument)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(CaseDocument::getUploadTimestamp,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(null);
    }
}
