package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        "Issue Date, fast track decision or hearingDate is empty";

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

    private boolean isHearingOutsideOfTimeline(final LocalDate min, final LocalDate max,
                                                      final LocalDate date) {
        return date.isBefore(min) || date.isAfter(max);
    }
}
