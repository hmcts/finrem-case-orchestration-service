package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NoticeOfChangeService;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;

@RestController
@Slf4j
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class NoticeOfChangeController implements BaseController {

    private final NoticeOfChangeService noticeOfChangeService;
    private static final String INCLUDES_REPRESENTATION_CHANGE = "updateIncludesRepresentativeChange";

    @PostMapping(path = "/representation-change", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles update Contested Case details where update includes a change in representation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> handleRepresentationChange(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseDetails originalCaseDetails = ccdRequest.getCaseDetailsBefore();
        Map<String, Object> caseData = caseDetails.getData();
        validateCaseData(ccdRequest);


        log.info("Received request to update representation on case with Case ID: {}", caseDetails.getId());
        caseData = noticeOfChangeService.caseWorkerUpdatesRepresentation(caseDetails, authToken, originalCaseDetails);
        caseDetails.getData().putAll(caseData);


        log.info("Updated details for caseID {}: {}", caseDetails.getId(), caseData);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
