package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        if (issueDate.isEmpty() || !(hearingType.equals(HearingType.FDA) || hearingType.equals(HearingType.FDR))) {
            return List.of();
        }

        if (caseData.isFastTrackApplication()) {
            if (isHearingOutsideOfTimeline(issueDate.get().plusWeeks(6), issueDate.get().plusWeeks(10), hearingDate)) {
                return List.of(DATE_BETWEEN_6_AND_10_WEEKS);
            }
        } else if (expressCaseService.isExpressCase(caseData)) {
            if (isHearingOutsideOfTimeline(issueDate.get().plusWeeks(16), issueDate.get().plusWeeks(20), hearingDate)) {
                return List.of(DATE_BETWEEN_16_AND_20_WEEKS);
            }
        } else if (isHearingOutsideOfTimeline(issueDate.get().plusWeeks(12), issueDate.get().plusWeeks(16), hearingDate)) {
            return List.of(DATE_BETWEEN_12_AND_16_WEEKS);
        }

        return List.of();
    }

    // todo docs
    // todo tests
    public List<String> validateGeneralApplicationDirectionsMandatoryParties(FinremCaseData caseData) {

        Set<String> codes = getSelectedPartyCodesForWorkingHearing(caseData);

        boolean bothSelected = codes.contains("[APPSOLICITOR]") && codes.contains("[RESPSOLICITOR]");
        return bothSelected ? List.of() : List.of(GENERAL_APPLICATION_DIRECTIONS_PARTY_ERROR);
    }

    // todo docs
    // todo tests
    public List<String> validateGeneralApplicationDirectionsNoticeSelection(FinremCaseData caseData) {
        // todo - null safe
        boolean sendingHearingNotice = YesOrNo.YES.equals(caseData.getManageHearingsWrapper().getWorkingHearing().getHearingNoticePrompt());
        return sendingHearingNotice ? List.of() : List.of(GENERAL_APPLICATION_DIRECTIONS_NOTICE_ERROR);
    }

    // todo docs
    // todo tests
    // From a data perspective, for each General Application there is generalApplicationSender.  This is the party that
    // created the hearing. This party can be Applicant, Respondent or Intervener1-4.  This party is used to build a label
    // so that the current user can select the correct General Application from a dropdown list.
    public List<String> validateGeneralApplicationDirectionsIntervenerParties(FinremCaseData caseData) {

        // todo - look for consts
        final Set <String> intervenerStrings = Set.of(
            "INTERVENER1",
            "INTERVENER2",
            "INTERVENER3",
            "INTERVENER4",
            "INTERVENER5"
        );

        // todo - null safe
        String selectedGeneralApplicationLabel = caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList().getValue().getLabel().toUpperCase();

        boolean intervenerCreatedGeneralApplication = false;

        // todo - consider redundant null check
        if (selectedGeneralApplicationLabel != null) {
            for (String intervener : intervenerStrings) {
                if (selectedGeneralApplicationLabel.contains(intervener)) {
                    intervenerCreatedGeneralApplication = true;
                    break;
                }
            }
        }

        if (intervenerCreatedGeneralApplication) {

            Set<String> codes = getSelectedPartyCodesForWorkingHearing(caseData);

            // todo - look for consts
            boolean anIntervenerIsSelected =
                codes.contains("[INTVRSOLICITOR1]") ||
                codes.contains("[INTVRSOLICITOR2]") ||
                codes.contains("[INTVRSOLICITOR3]") ||
                codes.contains("[INTVRSOLICITOR4]");

            return anIntervenerIsSelected ? List.of() : List.of(GENERAL_APPLICATION_DIRECTIONS_INTERVENER_WARNING);
        }

        return List.of();
    }

    private boolean isHearingOutsideOfTimeline(final LocalDate min, final LocalDate max,
                                                      final LocalDate date) {
        return date.isBefore(min) || date.isAfter(max);
    }

    /**
     * Retrieves the set of party codes selected for the working hearing in the provided case data.
     * <p>
     * Firstly, creates a list of {@link DynamicMultiSelectListElement} objects for the selected parties.
     * Secondly, get the codes for these parties, then returns that in a set.
     * </p>
     *
     * @param caseData the case data containing the manage hearings wrapper and working hearing details
     * @return a set of selected party codes; never {@code null}
     */
    private static Set<String> getSelectedPartyCodesForWorkingHearing(FinremCaseData caseData) {
        List<DynamicMultiSelectListElement> selected = Optional.ofNullable(caseData)
            .map(FinremCaseData::getManageHearingsWrapper)
            .map(ManageHearingsWrapper::getWorkingHearing)
            .map(WorkingHearing::getPartiesOnCaseMultiSelectList)
            .map(DynamicMultiSelectList::getValue)
            .orElse(Collections.emptyList());

        return selected.stream()
            .map(DynamicMultiSelectListElement::getCode)
            .collect(Collectors.toSet());
    }
}
