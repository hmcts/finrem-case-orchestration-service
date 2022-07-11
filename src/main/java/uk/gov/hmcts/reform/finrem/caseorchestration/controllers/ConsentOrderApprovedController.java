package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionTypeCollection;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class ConsentOrderApprovedController extends BaseController {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;
    private final NotificationService notificationService;
    private final FeatureToggleService featureToggleService;
    private final FinremCallbackRequestDeserializer callbackRequestDeserializer;

    @PostMapping(path = "/documents/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "'Consent Order Approved' callback handler. Generates relevant Consent Order Approved documents")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(source);

        validateCaseData(callbackRequest);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        Document latestConsentOrder = caseDetails.getCaseData().getLatestConsentOrder();

        if (!isEmpty(latestConsentOrder)) {
            generateAndPrepareDocuments(authToken, caseDetails);
        } else {
            log.info("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for case: {}",
                caseDetails.getId());
        }

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getCaseData())
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build());
    }

    @PostMapping(path = "/consent-in-contested/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "'Consent Order Approved' callback handler for consent in contested. Stamps Consent Order Approved documents"
        + "and adds them to a collection")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedConsentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

        CallbackRequest callbackRequest =
            callbackRequestDeserializer.deserialize(source);
        validateCaseData(callbackRequest);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, authToken);
        consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(caseDetails, authToken);

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build());
    }

    @PostMapping(path = "/consent-in-contested/send-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "'Consent Order Approved' and 'Consent Order Not Approved' callback handler for consent in contested. "
        + "Checks state and if not/approved generates docs else puts latest general order into uploadOrder fields. "
        + "Then sends the data to bulk print")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedSendOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

        CallbackRequest callback = callbackRequestDeserializer.deserialize(source);
        FinremCaseDetails caseDetails = callback.getCaseDetails();

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getCaseData())
            .build());
    }

    private void generateAndPrepareDocuments(String authToken, FinremCaseDetails caseDetails) {
        log.info("Generating and preparing documents for latest consent order, case {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getCaseData();
        Document latestConsentOrder = caseData.getLatestConsentOrder();

        Document approvedConsentOrderLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, authToken);
        Document consentOrderAnnexStamped = genericDocumentService.annexStampDocument(latestConsentOrder, authToken);

        ConsentOrder approvedOrder = ConsentOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped).build();

        if (!getPensionDocumentsData(caseData).isEmpty()) {
            handlePopulatedPensionDocuments(authToken, caseDetails, caseData, approvedOrder);
        }

        List<ConsentOrderCollection> approvedOrders = singletonList(ConsentOrderCollection.builder()
            .value(approvedOrder).build());

        log.info("Generated ApprovedOrders = {} for case {}", approvedOrders, caseDetails.getId());
        caseData.setApprovedOrderCollection(approvedOrders);
        log.info("Successfully generated documents for 'Consent Order Approved' for case {}", caseDetails.getId());

        if (getPensionDocumentsData(caseData).isEmpty()) {
            handleEmptyPensionDocuments(authToken, caseDetails, caseData);
        }
    }

    private void handlePopulatedPensionDocuments(String authToken, FinremCaseDetails caseDetails, FinremCaseData caseData, ConsentOrder approvedOrder) {
        log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder for case {}",
            caseDetails.getId());

        List<PensionTypeCollection> stampedPensionDocs = consentOrderApprovedDocumentService.stampPensionDocuments(
            caseData.getPensionCollection(), authToken);

        log.info("Generated StampedPensionDocs = {} for case {}", stampedPensionDocs, caseDetails.getId());
        approvedOrder.setPensionDocuments(stampedPensionDocs);
    }

    private void handleEmptyPensionDocuments(String authToken, FinremCaseDetails caseDetails, FinremCaseData caseData) {
        log.info("Case {} has no pension documents, updating status to {} and sending for bulk print", caseDetails.getId(),
            CONSENT_ORDER_MADE);
        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);
        caseData.setState(CONSENT_ORDER_MADE.toString());
        notificationService.sendConsentOrderAvailableCtscEmail(caseDetails);

        if (caseData.isApplicantSolicitorAgreeToReceiveEmails()) {
            log.info("case - {}: Sending email notification for to Applicant Solicitor for 'Consent Order Available'", caseDetails.getId());
            notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
        }

        if (featureToggleService.isRespondentJourneyEnabled()
            && caseData.isRespondentSolicitorAgreeToReceiveEmails()) {
            log.info("case - {}: Sending email notification to Respondent Solicitor for 'Consent Order Available'", caseDetails.getId());
            notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
        }
    }

    private List<Document> getPensionDocumentsData(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getPensionCollection()).orElse(emptyList())
            .stream()
            .map(PensionTypeCollection::getValue)
            .map(PensionType::getUploadedDocument)
            .filter(Objects::nonNull)
            .collect(toList());
    }
}
