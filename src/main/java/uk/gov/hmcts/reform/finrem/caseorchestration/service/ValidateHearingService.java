package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private boolean isHearingOutsideOfTimeline(final LocalDate min, final LocalDate max,
                                                      final LocalDate date) {
        return date.isBefore(min) || date.isAfter(max);
    }
}
