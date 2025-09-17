package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService.HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateHearingService {

    public static final String DATE_BETWEEN_6_AND_10_WEEKS =
        "Date of the Fast Track hearing must be between 6 and 10 weeks.";
    public static final String DATE_BETWEEN_12_AND_16_WEEKS =
        "Date of the hearing must be between 12 and 16 weeks.";
    public static final String DATE_BETWEEN_16_AND_20_WEEKS =
        "Date of the express pilot hearing should be between 16 and 20 weeks.";
    public static final String REQUIRED_FIELD_EMPTY_ERROR =
        "Issue Date, fast track decision or hearing date is empty";
    public static final String GENERAL_APPLICATION_DIRECTIONS_INTERVENER_WARNING =
        "An Intervener created this general application. Consider if an Intervener should be selected in \"Who should see this order?\"";
    public static final String GENERAL_APPLICATION_DIRECTIONS_PARTY_ERROR =
        "Select Applicant and Respondent for \"Who should see this order?\"";
    public static final String GENERAL_APPLICATION_DIRECTIONS_NOTICE_ERROR =
        "Select \"Yes\" for \"Do you want to send a notice of hearing?\"";

    private final SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;
    private final ExpressCaseService expressCaseService;

    public List<String> validateHearingErrors(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();

        List<String> errors = new ArrayList<>();
        if (caseData.getIssueDate() == null
            || caseData.getListForHearingWrapper().getHearingDate() == null
            || caseData.getFastTrackDecision() == null) {
            errors.add(REQUIRED_FIELD_EMPTY_ERROR);
        }

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        errors.addAll(selectablePartiesCorrespondenceService
            .validateApplicantAndRespondentCorrespondenceAreSelected(finremCaseDetails.getData(),
                HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE));
        return errors;
    }

    public List<String> validateHearingWarnings(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        LocalDate issueDate = caseData.getIssueDate();
        LocalDate hearingDate = caseData.getListForHearingWrapper().getHearingDate();

        if (caseData.isFastTrackApplication()) {
            if (isHearingOutsideOfTimeline(issueDate.plusWeeks(6), issueDate.plusWeeks(10), hearingDate)) {
                return List.of(DATE_BETWEEN_6_AND_10_WEEKS);
            }
        } else if (expressCaseService.isExpressCase(caseData)) {
            if (isHearingOutsideOfTimeline(issueDate.plusWeeks(16), issueDate.plusWeeks(20), hearingDate)) {
                return List.of(DATE_BETWEEN_16_AND_20_WEEKS);
            }
        } else if (isHearingOutsideOfTimeline(issueDate.plusWeeks(12), issueDate.plusWeeks(16), hearingDate)) {
            return List.of(DATE_BETWEEN_12_AND_16_WEEKS);
        }
        return List.of();
    }

    /**
     * Validates if any required fields for managing a hearing are empty.
     *
     * @param caseData the case data containing hearing details to validate
     * @return a list of error messages if required fields are empty, otherwise an empty list
     */
    public List<String> validateManageHearingErrors(FinremCaseData caseData) {
        boolean isAnyFieldEmpty = caseData.getIssueDate() == null
            || caseData.getFastTrackDecision() == null;

        return isAnyFieldEmpty ? List.of(REQUIRED_FIELD_EMPTY_ERROR) : List.of();
    }

    /**
     * Validates if the hearing date for a specific hearing type falls within the expected timeline
     * based on the case type and application type (e.g., fast track or express case).
     *
     * @param caseData the case data containing hearing details to validate
     * @param hearingType the type of hearing to validate (e.g., FDA, FDR)
     * @return a list of warning messages if the hearing date is outside the expected timeline,
     *         otherwise an empty list
     */
    public List<String> validateManageHearingWarnings(FinremCaseData caseData, HearingType hearingType) {
        Optional<LocalDate> issueDate = Optional.ofNullable(caseData.getIssueDate());
        LocalDate hearingDate = caseData.getManageHearingsWrapper().getWorkingHearing().getHearingDate();

        if (issueDate.isEmpty()) {
            return List.of();
        }

        if (hearingType.equals(HearingType.FDA)
            && !expressCaseService.isExpressCase(caseData)) {
            // Validate Standard hearing timeline
            if (!caseData.isFastTrackApplication()
                && isHearingOutsideOfTimeline(issueDate.get().plusWeeks(12), issueDate.get().plusWeeks(16), hearingDate)) {
                return List.of(DATE_BETWEEN_12_AND_16_WEEKS);
            // Validate Fast Track hearing timeline
            } else if (caseData.isFastTrackApplication()
                && isHearingOutsideOfTimeline(issueDate.get().plusWeeks(6), issueDate.get().plusWeeks(10), hearingDate)) {
                return List.of(DATE_BETWEEN_6_AND_10_WEEKS);
            }
            // Validate Express hearing timeline
        } else if (hearingType.equals(HearingType.FDR)
            && expressCaseService.isExpressCase(caseData)
            && isHearingOutsideOfTimeline(issueDate.get().plusWeeks(16), issueDate.get().plusWeeks(20), hearingDate)) {
            return List.of(DATE_BETWEEN_16_AND_20_WEEKS);
        }

        return List.of();
    }

    /*
     * Used by the General Application Directions mid-event Handler.
     * Validates that if a hearing is required for the general application, the user has selected both
     * applicant and respondent parties.
     * @param caseData the case data containing hearing and general application details to validate
     * @return a list of error messages if either applicant or respondent party is not selected,
     */
    public List<String> validateGeneralApplicationDirectionsMandatoryParties(FinremCaseData caseData) {
        Set<String> codes = getSelectedPartyCodesForWorkingHearing(caseData);

        boolean bothSelected = codes.contains(APP_SOLICITOR.getCcdCode()) && codes.contains(RESP_SOLICITOR.getCcdCode());

        return bothSelected ? List.of() : List.of(GENERAL_APPLICATION_DIRECTIONS_PARTY_ERROR);
    }

    /*
     * Used by the General Application Directions mid-event Handler.
     * Validates that if a hearing is required for the general application, the user has selected to send a notice of hearing.
     * @param caseData the case data containing hearing and general application details to validate
     * @return a list of error messages if the user has not selected to send a notice
     */
    public List<String> validateGeneralApplicationDirectionsNoticeSelection(FinremCaseData caseData) {
        boolean yesChosenForSendHearingNotice = Optional.ofNullable(caseData.getManageHearingsWrapper().getWorkingHearing())
            .map(h -> YesOrNo.YES.equals(h.getHearingNoticePrompt()))
            .orElse(false);

        return yesChosenForSendHearingNotice ? List.of() : List.of(GENERAL_APPLICATION_DIRECTIONS_NOTICE_ERROR);
    }

    /*
     * Used by the General Application Directions mid-event Handler.
     * Validates that if an intervener created the selected general application, at least one intervener party is selected
     * to see the hearing correspondence.  Manage interveners can run after GA creation, which is why this is a lenient warning.
     * @param caseData the case data containing hearing and general application details to validate
     * @return a list of warning messages if no intervener party is selected for an intervener-created general application,
                otherwise an empty list
     */
    public List<String> validateGeneralApplicationDirectionsIntervenerParties(FinremCaseData caseData) {
        boolean intervenerCreatedGeneralApplication = didIntervenerCreateSelectedGeneralApplication(caseData);

        if (intervenerCreatedGeneralApplication) {
            Set<String> selectedHearingParties = getSelectedPartyCodesForWorkingHearing(caseData);
            Set<String> intervenerPartyList = Set.of(
                INTVR_SOLICITOR_1.getCcdCode(),
                INTVR_SOLICITOR_2.getCcdCode(),
                INTVR_SOLICITOR_3.getCcdCode(),
                INTVR_SOLICITOR_4.getCcdCode());
            boolean anIntervenerIsSelectedForGaHearing = selectedHearingParties.stream().anyMatch(intervenerPartyList::contains);
            return anIntervenerIsSelectedForGaHearing ? List.of() : List.of(GENERAL_APPLICATION_DIRECTIONS_INTERVENER_WARNING);
        }

        return List.of();
    }

    private boolean isHearingOutsideOfTimeline(final LocalDate min, final LocalDate max,
                                               final LocalDate date) {
        return date.isBefore(min) || date.isAfter(max);
    }

    /**
     * Retrieves the set of party codes selected for the working hearing in the provided event data.
     * Firstly, creates a list of {@link DynamicMultiSelectListElement} objects for the selected parties.
     * Secondly, get the codes for these parties, then returns that in a set.
     *
     * @param caseData the case data containing the manage hearings wrapper and working hearing details
     * @return a set of selected party codes; never {@code null}
     */
    private static Set<String> getSelectedPartyCodesForWorkingHearing(FinremCaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(FinremCaseData::getManageHearingsWrapper)
            .map(ManageHearingsWrapper::getWorkingHearing)
            .map(WorkingHearing::getSelectedPartyCodesForWorkingHearing)
            .orElse(Set.of());
    }

    /*
     * Specific to case data from the General Application Directions mid-event Handler.
     * Used to see if passed event data has a general application selected in the dynamic list called generalApplicationDirectionsList.
     * If so, check if the label for the value selected.  If the uppercase label contains the word "INTERVENER".  Then the
     * General Application was created by an intervener.
     * @param caseData the case data
     * @return true if the selected general application was created by an Intervener
     */
    private static boolean didIntervenerCreateSelectedGeneralApplication(FinremCaseData caseData) {
        String selectedGeneralApplicationLabel = "";
        DynamicList generalApplicationDirectionsList = caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList();

        if (generalApplicationDirectionsList != null && generalApplicationDirectionsList.getValue() != null) {
            selectedGeneralApplicationLabel =
                generalApplicationDirectionsList.getValue().getLabel();
        }

        return containsIgnoreCase(selectedGeneralApplicationLabel, "intervener");
    }
}
