package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import javax.validation.constraints.NotNull;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMOUNT_TO_PAY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_SUMMARY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class PBAPaymentController implements BaseController {

    private final FeeService feeService;
    private final PBAPaymentService pbaPaymentService;
    private final CaseDataService caseDataService;
    private final AssignCaseAccessService assignCaseAccessService;
    private final CcdDataStoreService ccdDataStoreService;
    private final FeatureToggleService featureToggleService;
    private final PrdOrganisationService prdOrganisationService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/pba-payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles PBA Payments for Consented Journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> pbaPayment(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for PBA payment for consented for Case ID: {}", caseDetails.getId());

        validateCaseData(callbackRequest);

        final Map<String, Object> mapOfCaseData = caseDetails.getData();
        feeLookup(authToken, callbackRequest, mapOfCaseData);

        if (isPBAPayment(mapOfCaseData)) {
            if (isPBAPaymentReferenceDoesNotExists(mapOfCaseData)) {
                PaymentResponse paymentResponse = pbaPaymentService.makePayment(authToken, caseDetails);
                if (!paymentResponse.isPaymentSuccess()) {
                    return paymentFailure(mapOfCaseData, paymentResponse);
                }
                mapOfCaseData.put(PBA_PAYMENT_REFERENCE, paymentResponse.getReference());
                log.info("Consented Payment Succeeded.");
            } else {
                log.info("PBA Payment Reference for Consented case already exists.");
            }
        } else {
            log.info("Not PBA Payment - Moving state to Awaiting HWF Decision");
            mapOfCaseData.put(STATE, AWAITING_HWF_DECISION.toString());
        }

        if (featureToggleService.isAssignCaseAccessEnabled()) {
            try {
                String applicantOrgId = getApplicantOrgId(caseDetails);

                if (applicantOrgId != null) {
                    OrganisationsResponse prdOrganisation = prdOrganisationService.retrieveOrganisationsData(authToken);

                    if (prdOrganisation.getOrganisationIdentifier().equals(applicantOrgId)) {
                        log.info("Assigning case access for Case ID: {}", caseDetails.getId());
                        ccdDataStoreService.removeCreatorRole(caseDetails, authToken);
                        try {
                            assignCaseAccessService.assignCaseAccess(caseDetails, authToken);
                        } catch (Exception e) {
                            return assignCaseAccessFailure(caseDetails);
                        }
                    } else {
                        return assignCaseAccessFailure(caseDetails);
                    }
                } else {
                    return assignCaseAccessFailure(caseDetails);
                }
            } catch (Exception e) {
                return assignCaseAccessFailure(caseDetails);
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(mapOfCaseData).build());
    }

    private String getApplicantOrgId(CaseDetails caseDetails) {
        Map<String, Object> applicantOrgPolicy = (Map<String, Object>) caseDetails.getData().get(ORGANISATION_POLICY_APPLICANT);
        if (applicantOrgPolicy != null) {
            Map<String, Object> applicantOrganisation = (Map<String, Object>) applicantOrgPolicy.get(ORGANISATION_POLICY_ORGANISATION);

            if (applicantOrganisation != null) {
                return (String) applicantOrganisation.get(ORGANISATION_POLICY_ORGANISATION_ID);
            }
        }

        return null;
    }

    private ResponseEntity<AboutToStartOrSubmitCallbackResponse> assignCaseAccessFailure(CaseDetails caseDetails) {
        log.error("Assigning case access failed for Case ID: {}", caseDetails.getId());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.of("Failed to assign applicant solicitor to case, "
                + "please ensure you have selected the correct applicant organisation on case"))
            .build());
    }

    private void feeLookup(@RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
                           @RequestBody CallbackRequest callbackRequest, Map<String, Object> caseData) {
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> feeResponse = new FeeLookupController(feeService, caseDataService)
            .feeLookup(authToken, callbackRequest);
        caseData.put(ORDER_SUMMARY, Objects.requireNonNull(feeResponse.getBody()).getData().get(ORDER_SUMMARY));
        caseData.put(AMOUNT_TO_PAY, Objects.requireNonNull(feeResponse.getBody()).getData().get(AMOUNT_TO_PAY));
    }

    private ResponseEntity<AboutToStartOrSubmitCallbackResponse> paymentFailure(Map<String, Object> caseData, PaymentResponse paymentResponse) {
        String paymentError = paymentResponse.getPaymentError();
        log.info("Payment by PBA number {} failed, payment error : {} ", caseData.get(PBA_NUMBER), paymentResponse.getPaymentError());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.of(paymentError))
            .build());
    }
}
