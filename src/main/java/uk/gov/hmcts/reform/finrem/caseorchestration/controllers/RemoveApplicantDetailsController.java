package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class RemoveApplicantDetailsController implements BaseController {

    @PostMapping(path = "/remove-details", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Removes applicant details or applicants solicitor details")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> removeDetails(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        log.info("Received request for removing applicant / applicants solicitor details. "
                        + "Auth token: {}, Case request : {}",
                authorisationToken, callback);

        validateCaseData(callback);

        Map<String, Object> caseData = callback.getCaseDetails().getData();
        String applicantRepresented = caseData.get("applicantRepresented").toString();

        if (applicantRepresented.equals("Yes")) {
            //remove applicants data as solicitors data has been added
            caseData.remove("applicantAddress");
            caseData.remove("applicantPhone");
            caseData.remove("applicantEmail");
            caseData.remove("applicantFMName");
            caseData.remove("applicantLName");
        } else {
            caseData.remove("applicantSolicitorName");
            caseData.remove("applicantSolicitorFirm");
            caseData.remove("applicantSolicitorAddress");
            caseData.remove("applicantSolicitorPhone");
            caseData.remove("applicantSolicitorEmail");
            caseData.remove("applicantSolicitorDXnumber");
            caseData.remove("applicantSolicitorConsentForEmails");
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
