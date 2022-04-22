package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateRepresentationService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class UpdateRepresentationController implements BaseController {

    @Autowired
    private UpdateRepresentationService updateRepresentationService;

    @Autowired
    private AssignCaseAccessService assignCaseAccessService;

    @PostMapping(path = "/apply-noc-decision", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Applies Notice of Change Decision when initiated by solicitor and saves new sol's details to case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateRepresentation(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to apply Notice of Change Decision and update representation for case {}",
            caseDetails.getId());

        validateCaseData(ccdRequest);

        caseDetails.getData().putAll(updateRepresentationService.updateRepresentationAsSolicitor(caseDetails, authToken));
        log.info("Case details for caseID {} == {}", caseDetails.getId(), caseDetails.getData());
        AboutToStartOrSubmitCallbackResponse response = assignCaseAccessService.applyDecision(authToken, caseDetails);
        log.info("Case details after decision applied: {}", response);
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("ChangeOfRepresentatives", response.getData().get("ChangeOfRepresentatives"));
        caseData.put("ApplicantOrganisationPolicy", response.getData().get("ApplicantOrganisationPolicy"));
        caseData.put("RespondentOrganisationPolicy", response.getData().get("RespondentOrganisationPolicy"));
        caseData.put("changeOrganisationRequestField", ChangeOfRepresentationRequest.builder().build());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
