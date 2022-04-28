package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import javax.validation.constraints.NotNull;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class RemoveApplicantDetailsController implements BaseController {

    private static final String INCLUDES_REPRESENTATION_CHANGE = "updateIncludesRepresentativeChange";

    @Autowired final NoticeOfChangeService noticeOfChangeService;

    @Autowired final AssignCaseAccessService assignCaseAccessService;

    @Autowired final SystemUserService systemUserService;

    @PostMapping(path = "/remove-details", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Removes applicant details or applicants solicitor details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> removeDetails(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for removing Applicant/Applicants Solicitor details for Case ID: {}", caseDetails.getId());

        validateCaseData(callback);

        Map<String, Object> caseData = caseDetails.getData();
        String applicantRepresented = caseData.get(APPLICANT_REPRESENTED).toString();
        boolean isContested = caseDetails.getCaseTypeId().equalsIgnoreCase(CASE_TYPE_ID_CONTESTED);
        String respondentRepresented = isContested
            ? (String) caseData.get(CONTESTED_RESPONDENT_REPRESENTED)
            : (String) caseData.get(CONSENTED_RESPONDENT_REPRESENTED);

        if (applicantRepresented.equals(YES_VALUE)) {
            //remove applicants data as solicitors data has been added
            caseData.remove("applicantAddress");
            caseData.remove("applicantPhone");
            caseData.remove("applicantEmail");
        } else {
            caseData.remove("applicantSolicitorName");
            caseData.remove("applicantSolicitorFirm");
            caseData.remove("applicantSolicitorAddress");
            caseData.remove("applicantSolicitorPhone");
            caseData.remove("applicantSolicitorEmail");
            caseData.remove("applicantSolicitorDXnumber");
            caseData.remove("applicantSolicitorConsentForEmails");
        }

        if (respondentRepresented.equals(YES_VALUE)) {
            caseData.remove(RESPONDENT_ADDRESS);
            caseData.remove(RESPONDENT_PHONE);
            caseData.remove(RESPONDENT_EMAIL);
        } else {
            caseData.remove(RESP_SOLICITOR_NAME);
            caseData.remove(RESP_SOLICITOR_FIRM);
            caseData.remove(RESP_SOLICITOR_ADDRESS);
            caseData.remove(RESP_SOLICITOR_PHONE);
            caseData.remove(RESP_SOLICITOR_EMAIL);
            caseData.remove(RESP_SOLICITOR_DX_NUMBER);
            caseData.remove(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT);
        }

        if (caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE).equals(YES_VALUE)) {
            CaseDetails originalCaseDetails = callback.getCaseDetailsBefore();
            log.info("Received request to update representation on case with Case ID: {}", caseDetails.getId());
            caseData = noticeOfChangeService.updateRepresentation(caseDetails, authorisationToken, originalCaseDetails);
            caseDetails.getData().putAll(caseData);

            AboutToStartOrSubmitCallbackResponse response = assignCaseAccessService.applyDecision(
                systemUserService.getSysUserToken(),
                caseDetails);

            log.info("Response from Manage case service for caseID {}: {}", caseDetails.getId(), response);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
