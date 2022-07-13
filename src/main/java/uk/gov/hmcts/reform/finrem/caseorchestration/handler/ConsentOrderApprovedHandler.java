package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionTypeCollection;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderApprovedHandler {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;
    private final NotificationService notificationService;
    private final FeatureToggleService featureToggleService;

    public AboutToStartOrSubmitCallbackResponse handleConsentOrderApproved(CallbackRequest callbackRequest,
                                                                           String authToken) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (!isEmpty(caseDetails.getCaseData().getLatestConsentOrder())) {
            generateAndPrepareDocuments(authToken, caseDetails);
        } else {
            log.info("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for case: {}",
                caseDetails.getId());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getCaseData())
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build();
    }

    public AboutToStartOrSubmitCallbackResponse handleConsentInContestConsentOrderApproved(CallbackRequest callbackRequest,
                                                                                           String authToken) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();
        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, authToken);
        consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(caseDetails, authToken);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build();
    }

    public  AboutToStartOrSubmitCallbackResponse handleConsentInContestSendOrder(CallbackRequest callbackRequest,
                                                                                 String authToken) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getCaseData())
            .build();
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

        List<ConsentOrderCollection> approvedOrders = singletonList(ConsentOrderCollection.builder().value(approvedOrder).build());

        log.info("Generated ApprovedOrders = {} for case {}", approvedOrders, caseDetails.getId());
        caseData.setApprovedOrderCollection(approvedOrders);
        log.info("Successfully generated documents for 'Consent Order Approved' for case {}", caseDetails.getId());

        if (getPensionDocumentsData(caseData).isEmpty()) {
            handleEmptyPensionDocuments(authToken, caseDetails, caseData);
        }
    }

    private void handlePopulatedPensionDocuments(String authToken, FinremCaseDetails caseDetails,
                                                 FinremCaseData caseData, ConsentOrder approvedOrder) {
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
        caseData.setState(CONSENT_ORDER_MADE.getId());
        notificationService.sendConsentOrderAvailableCtscEmail(caseDetails);

        if (caseData.isApplicantSolicitorAgreeToReceiveEmails()) {
            log.info("case - {}: Sending email notification for to Applicant Solicitor for 'Consent Order Available'",
                caseDetails.getId());
            notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
        }

        if (featureToggleService.isRespondentJourneyEnabled()
            && caseData.isRespondentSolicitorAgreeToReceiveEmails()) {
            log.info("case - {}: Sending email notification to Respondent Solicitor for 'Consent Order Available'",
                caseDetails.getId());
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
