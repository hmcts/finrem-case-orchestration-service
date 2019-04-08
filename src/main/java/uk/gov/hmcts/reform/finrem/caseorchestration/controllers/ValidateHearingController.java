package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import java.time.LocalDate;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class ValidateHearingController implements BaseController {


    private final PBAValidationService pbaValidationService;

    public static boolean isDateInBetweenIncludingEndPoints(final LocalDate min, final LocalDate max,
                                                            final LocalDate date) {
        return !(date.isBefore(min) || date.isAfter(max));
    }

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/validate-hearing", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> validateHearing(
        @RequestHeader(value = "Authorization", required = false) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request for validating a hearing. Auth token: {}, Case request : {}", authToken,
            callbackRequest);

        validateCaseData(callbackRequest);

        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        String issueDate = ObjectUtils.toString(caseData.get(ISSUE_DATE));
        String hearingDate = ObjectUtils.toString(caseData.get(HEARING_DATE));
        String fastTrackDecision = ObjectUtils.toString(caseData.get(FAST_TRACK_DECISION));

        if (StringUtils.isBlank(issueDate) || StringUtils.isBlank(fastTrackDecision)
            || StringUtils.isBlank(hearingDate)) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of("Issue Date , fast track decision or hearingDate is empty"))
                .build());
        }

        LocalDate issueLocalDate = LocalDate.parse(issueDate);
        LocalDate hearingLocalDate = LocalDate.parse(hearingDate);

        if (fastTrackDecision.equalsIgnoreCase("yes")) {
            if (!isDateInBetweenIncludingEndPoints(issueLocalDate.plusWeeks(6), issueLocalDate.plusWeeks(10),
                hearingLocalDate)) {
                return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                    .warnings(ImmutableList.of("Date of the Fast Track hearing must be between 6 and 10 weeks."))
                    .build());
            }
        } else if (!isDateInBetweenIncludingEndPoints(issueLocalDate.plusWeeks(12), issueLocalDate.plusWeeks(14),
            hearingLocalDate)) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .warnings(ImmutableList.of("Date of the hearing must be between 12 and 14 weeks."))
                .build());
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().build());
    }
}
