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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateRepresentationWorkflowService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATION_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class UpdateConsentedCaseController implements BaseController {

    private static final String DIVORCE_STAGE_REACHED = "divorceStageReached";
    private static final String DIVORCE_UPLOAD_EVIDENCE_2 = "divorceUploadEvidence2";
    private static final String DIVORCE_DECREE_ABSOLUTE_DATE = "divorceDecreeAbsoluteDate";
    private static final String DIVORCE_UPLOAD_EVIDENCE_1 = "divorceUploadEvidence1";
    private static final String DIVORCE_DECREE_NISI_DATE = "divorceDecreeNisiDate";

    @Autowired private ConsentOrderService consentOrderService;

    @Autowired private UpdateRepresentationWorkflowService nocWorkflowService;

    @Autowired private FeatureToggleService featureToggleService;

    @PostMapping(path = "/update-case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles update case details and cleans up the data fields based on the options chosen for Consented Cases")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateCase(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update consented case with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);
        Map<String, Object> caseData = caseDetails.getData();

        updateDivorceDetails(caseData);
        updatePeriodicPaymentData(caseData);
        updatePropertyDetails(caseData);
        updateRespondentSolicitorAddress(caseData);
        updateD81Details(caseData);
        updateApplicantOrSolicitorContactDetails(caseData);
        updateLatestConsentOrder(ccdRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/update-contact-details", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles update case details and cleans up the data fields based on the options chosen for Consented Cases")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateDetails(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update consented case with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);
        Map<String, Object> caseData = caseDetails.getData();

        updateRespondentSolicitorAddress(caseData);
        updateApplicantOrSolicitorContactDetails(caseData);

        if (featureToggleService.isCaseworkerNoCEnabled()) {

            if (Optional.ofNullable(caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE)).isPresent()
                && caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE).equals(YES_VALUE)) {
                CaseDetails originalCaseDetails = ccdRequest.getCaseDetailsBefore();
                return ResponseEntity.ok(nocWorkflowService.handleNoticeOfChangeWorkflow(caseDetails,
                    authToken,
                    originalCaseDetails));
            }

        }

        persistOrgPolicies(caseData, ccdRequest.getCaseDetailsBefore());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void updateLatestConsentOrder(CallbackRequest callbackRequest) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        caseData.put(LATEST_CONSENT_ORDER, consentOrderService.getLatestConsentOrderData(callbackRequest));
    }

    private void updateDivorceDetails(Map<String, Object> caseData) {
        if (caseData.get(DIVORCE_STAGE_REACHED).equals("Decree Nisi")) {
            // remove Decree Absolute details
            caseData.put(DIVORCE_UPLOAD_EVIDENCE_2, null);
            caseData.put(DIVORCE_DECREE_ABSOLUTE_DATE, null);
        } else {
            // remove Decree Nisi details
            caseData.put(DIVORCE_UPLOAD_EVIDENCE_1, null);
            caseData.put(DIVORCE_DECREE_NISI_DATE, null);
        }
    }

    private void updatePeriodicPaymentData(Map<String, Object> caseData) {
        List natureOfApplication2 = (List) caseData.get("natureOfApplication2");

        if (hasNotSelected(natureOfApplication2, "Periodical Payment Order")) {
            removePeriodicPaymentData(caseData);
        } else {
            // if written agreement for order for children
            if (equalsTo((String) caseData.get("natureOfApplication5"), "Yes")) {
                caseData.put("natureOfApplication6", null);
                caseData.put("natureOfApplication7", null);
            }
        }
    }

    private void updatePropertyDetails(Map<String, Object> caseData) {
        List natureOfApplication2 = (List) caseData.get("natureOfApplication2");

        if (hasNotSelected(natureOfApplication2, "Property Adjustment Order")) {
            removePropertyAdjustmentDetails(caseData);
        }
    }

    private void updateD81Details(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("d81Question"), "Yes")) {
            caseData.put("d81Applicant", null);
            caseData.put("d81Respondent", null);
        } else {
            caseData.put("d81Joint", null);
        }
    }

    private void updateRespondentSolicitorAddress(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(CONSENTED_RESPONDENT_REPRESENTED), "No")) {
            removeRespondentSolicitorAddress(caseData);
        } else {
            removeRespondentAddress(caseData);
        }
    }

    private void removePeriodicPaymentData(Map<String, Object> caseData) {
        caseData.put("natureOfApplication5", null);
        caseData.put("natureOfApplication6", null);
        caseData.put("natureOfApplication7", null);
        caseData.put("orderForChildrenQuestion1", null);
    }

    private void removeRespondentSolicitorAddress(Map<String, Object> caseData) {
        caseData.put(RESP_SOLICITOR_NAME, null);
        caseData.put(RESP_SOLICITOR_FIRM, null);
        caseData.put(RESP_SOLICITOR_REFERENCE, null);
        caseData.put(RESP_SOLICITOR_ADDRESS, null);
        caseData.put(RESP_SOLICITOR_PHONE, null);
        caseData.put(RESP_SOLICITOR_EMAIL, null);
        caseData.put(RESP_SOLICITOR_DX_NUMBER, null);
    }

    private void removePropertyAdjustmentDetails(Map<String, Object> caseData) {
        caseData.put("natureOfApplication3a", null);
        caseData.put("natureOfApplication3b", null);
    }


    private void updateApplicantOrSolicitorContactDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(APPLICANT_REPRESENTED), "No")) {
            removeApplicantSolicitorAddress(caseData);
        } else {
            removeApplicantAddress(caseData);
        }
    }

    private void removeApplicantSolicitorAddress(Map<String, Object> caseData) {
        caseData.put(CONSENTED_SOLICITOR_NAME, null);
        caseData.put(CONSENTED_SOLICITOR_FIRM, null);
        caseData.put(SOLICITOR_REFERENCE, null);
        caseData.put(CONSENTED_SOLICITOR_ADDRESS, null);
        caseData.put(SOLICITOR_PHONE, null);
        caseData.put(SOLICITOR_EMAIL, null);
        caseData.put(CONSENTED_SOLICITOR_DX_NUMBER, null);
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, null);
    }

    private void removeApplicantAddress(Map<String, Object> caseData) {
        caseData.put(APPLICANT_ADDRESS, null);
        caseData.put(APPLICANT_PHONE, null);
        caseData.put(APPLICANT_EMAIL, null);
    }

    private void removeRespondentAddress(Map<String, Object> caseData) {
        caseData.put(RESPONDENT_ADDRESS, null);
        caseData.put(RESPONDENT_PHONE, null);
        caseData.put(RESPONDENT_EMAIL, null);
    }

    private boolean equalsTo(String fieldData, String value) {
        return nonNull(fieldData) && value.equalsIgnoreCase(fieldData.trim());
    }

    private boolean hasNotSelected(List<String> list, String option) {
        return nonNull(list) && !list.contains(option);
    }

    private void persistOrgPolicies(Map<String, Object> caseData, CaseDetails originalDetails) {
        caseData.put(APPLICANT_ORGANISATION_POLICY, originalDetails.getData().get(APPLICANT_ORGANISATION_POLICY));
        caseData.put(RESPONDENT_ORGANISATION_POLICY, originalDetails.getData().get(RESPONDENT_ORGANISATION_POLICY));
    }
}
