package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovedConsentOrderAboutToSubmitHandler implements CallbackHandler {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;
    private final NotificationService notificationService;

    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_APPLICATION.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("ConsentOrderApprovedAboutToSubmitHandle handle case Id {}", caseDetails.getId());

        Document latestConsentOrder = caseDetails.getCaseData().getLatestConsentOrder();

        if (!isEmpty(latestConsentOrder)) {
            generateAndPrepareDocuments(userAuthorisation, caseDetails, latestConsentOrder);
        } else {
            log.info("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for case: {}",
                caseDetails.getId());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build();
    }


    private void generateAndPrepareDocuments(String authToken, FinremCaseDetails caseDetails, Document latestConsentOrder) {
        log.info("Generating and preparing documents for latest consent order, case {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getCaseData();

        Document approvedConsentOrderLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, authToken);
        log.info("Approved consent order letter generated: Filename = {}, url = {}, binUrl = {}",
            approvedConsentOrderLetter.getFilename(), approvedConsentOrderLetter.getUrl(), approvedConsentOrderLetter.getBinaryUrl());
        Document consentOrderAnnexStamped = genericDocumentService.annexStampDocument(latestConsentOrder, authToken);

        ConsentOrder approvedOrder = ConsentOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped)
            .build();

        if (!getPensionCollectionDocuments(caseData).isEmpty()) {
            log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder for case {}",
                caseDetails.getId());

            List<PensionTypeCollection> stampedPensionDocs = consentOrderApprovedDocumentService.stampPensionDocuments(
                caseData.getPensionCollection(), authToken);
            log.info("Generated StampedPensionDocs = {} for case {}", stampedPensionDocs, caseDetails.getId());
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        List<ConsentOrderCollection> approvedOrders = singletonList(ConsentOrderCollection.builder()
            .value(approvedOrder)
            .build());
        log.info("Generated ApprovedOrders = {} for case {}", approvedOrders, caseDetails.getId());
        caseData.setApprovedOrderCollection(approvedOrders);
        log.info("Successfully generated documents for 'Consent Order Approved' for case {}", caseDetails.getId());

        if (getPensionCollectionDocuments(caseData).isEmpty()) {
            log.info("Case {} has no pension documents, updating status to {} and sending for bulk print",
                caseDetails.getId(), CONSENT_ORDER_MADE);
            consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);
            caseData.setState(State.CONSENT_ORDER_MADE.getStateId());
            notificationService.sendConsentOrderAvailableCtscEmail(caseDetails);

            if (notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
                log.info("case - {}: Sending email notification for to Applicant Solicitor for 'Consent Order Available'", caseDetails.getId());
                notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
            }
            if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
                log.info("case - {}: Sending email notification to Respondent Solicitor for 'Consent Order Available'", caseDetails.getId());
                notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
            }
        }
    }

    private List<Document> getPensionCollectionDocuments(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getPensionCollection()).orElse(new ArrayList<>())
            .stream()
            .map(PensionTypeCollection::getValue)
            .map(PensionType::getUploadedDocument)
            .filter(Objects::nonNull)
            .toList();
    }
}
