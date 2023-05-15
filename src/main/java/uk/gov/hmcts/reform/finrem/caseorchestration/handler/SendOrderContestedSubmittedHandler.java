package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;

@Slf4j
@Service
public class SendOrderContestedSubmittedHandler extends FinremCallbackHandler {
    private final NotificationService notificationService;
    private final GeneralOrderService generalOrderService;
    private final CcdService ccdService;
    private final BulkPrintService bulkPrintService;
    private final DocumentHelper documentHelper;
    private final GenericDocumentService genericDocumentService;

    public SendOrderContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              NotificationService notificationService,
                                              GeneralOrderService generalOrderService,
                                              CcdService ccdService,
                                              BulkPrintService bulkPrintService,
                                              DocumentHelper documentHelper,
                                              GenericDocumentService genericDocumentService) {
        super(finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.generalOrderService = generalOrderService;
        this.ccdService = ccdService;
        this.bulkPrintService = bulkPrintService;
        this.documentHelper = documentHelper;
        this.genericDocumentService = genericDocumentService;
    }


    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} submitted callback for case id: {}", callbackRequest.getEventType(), caseDetails.getId());

        List<String> parties = generalOrderService.getParties(caseDetails);
        log.info("Selected parties {} on case {}", parties, caseDetails.getId());

        DynamicMultiSelectList selectedDocs = caseDetails.getData().getOrdersToShare();
        log.info("Selected orders {} on case {}", selectedDocs, caseDetails.getId());

        log.info("Sending general order for case {}", caseDetails.getId());
        printAndMailGeneralOrderToParties(caseDetails, parties, selectedDocs, userAuthorisation);

        log.info("Sending hearing order for case {}", caseDetails.getId());
        List<CaseDocument> hearingOrders = generalOrderService.hearingOrderToProcess(caseDetails, selectedDocs);
        if (!hearingOrders.isEmpty()) {
            printAndMailHearingDocuments(caseDetails, hearingOrders, parties, userAuthorisation);
        }

        sendNotifications(callbackRequest, parties);

        updateCaseWithPostStateOption(caseDetails, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }

    private void printAndMailHearingDocuments(FinremCaseDetails caseDetails,
                                              List<CaseDocument> hearingOrders,
                                              List<String> partyList,
                                              String authorisationToken) {

        List<BulkPrintDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails, hearingOrders, authorisationToken);

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
            && partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
            log.info("Received request to send hearing pack for applicant for case {}:", caseDetails.getId());
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, hearingDocumentPack);
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)
            && partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
            log.info("Received request to send hearing pack for respondent for case {}:", caseDetails.getId());
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, hearingDocumentPack);
        }

    }

    private List<BulkPrintDocument> createHearingDocumentPack(FinremCaseDetails caseDetails,
                                                              List<CaseDocument> hearingOrders, String authorisationToken) {

        Long id = caseDetails.getId();
        FinremCaseData caseData = caseDetails.getData();
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseData.getOrderApprovedCoverLetter()));
        hearingOrders.forEach(order -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(order)));

        CaseDocument document = caseData.getAdditionalDocument();
        if (document != null) {
            CaseDocument caseDocument = genericDocumentService.convertDocumentIfNotPdfAlready(document, authorisationToken);
            log.info("additional uploaded document with send order {} for caseId {}", caseDocument, id);
            hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument));
            caseData.setAdditionalDocument(caseDocument);
        }

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(
                caseDocument -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument)));
        }

        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getHearingDocumentsAsBulkPrintDocuments(
            caseData, authorisationToken);

        if (!otherHearingDocuments.isEmpty()) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }
        return hearingDocumentPack;
    }



    private void printAndMailGeneralOrderToParties(FinremCaseDetails caseDetails,
                                                   List<String> partyList,
                                                   DynamicMultiSelectList selectedDocs,
                                                   String authorisationToken) {

        log.info("Print selected 'GeneralOrder' to selected parties for caseId {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        CaseDocument generalOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();

        if (generalOrder != null) {
            BulkPrintDocument generalOrderPrint = documentHelper.getCaseDocumentAsBulkPrintDocument(generalOrder);
            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
                && partyList.contains(CaseRole.APP_SOLICITOR.getValue())
                && isSelectedOrderMatches(selectedDocs, generalOrder.getDocumentFilename())) {
                log.info("Sending selected general order {} to applicant via bulk print for caseId {}",
                    generalOrder.getDocumentFilename(), caseDetails.getId());
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, singletonList(generalOrderPrint));
            }

            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)
                && partyList.contains(CaseRole.RESP_SOLICITOR.getValue())
                && isSelectedOrderMatches(selectedDocs, generalOrder.getDocumentFilename())) {

                log.info("Sending selected general order {} to respondent via bulk print for caseId {}",
                    generalOrder.getDocumentFilename(), caseDetails.getId());
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, singletonList(generalOrderPrint));
            }
        }
    }

    private boolean isSelectedOrderMatches(DynamicMultiSelectList selectedDocs, String documentName) {
        Optional<DynamicMultiSelectListElement> selectedOrder = isLatestGeneralOrderSelectedToShare(selectedDocs);
        return selectedOrder.map(dynamicMultiSelectListElement -> dynamicMultiSelectListElement.getCode()
            .equals(documentName)).orElse(false);
    }

    private Optional<DynamicMultiSelectListElement> isLatestGeneralOrderSelectedToShare(DynamicMultiSelectList selectedDocs) {
        List<DynamicMultiSelectListElement> selectedOrder = selectedDocs.getValue();
        return selectedOrder.stream()
            .filter(doc -> doc.getCode().equals(GENERAL_ORDER_LATEST_DOCUMENT)).findAny();
    }

    private void updateCaseWithPostStateOption(FinremCaseDetails caseDetails, String userAuthorisation) {

        SendOrderEventPostStateOption sendOrderPostStateOption = caseDetails.getData().getSendOrderPostStateOption();
        if (isOptionThatRequireUpdate(sendOrderPostStateOption)) {
            caseDetails.getData().setSendOrderPostStateOption(null);
            ccdService.executeCcdEventOnCase(
                userAuthorisation,
                String.valueOf(caseDetails.getId()),
                caseDetails.getCaseType().getCcdType(),
                sendOrderPostStateOption.getEventToTrigger().getCcdType());
        }
    }

    private boolean isOptionThatRequireUpdate(SendOrderEventPostStateOption postStateOption) {
        return postStateOption.getEventToTrigger().equals(EventType.PREPARE_FOR_HEARING)
            || postStateOption.getEventToTrigger().equals(EventType.CLOSE);
    }

    private void sendNotifications(FinremCallbackRequest callbackRequest, List<String> parties) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        String caseId = String.valueOf(caseDetails.getId());

        if (Objects.nonNull(caseData.getFinalOrderCollection())) {
            log.info("Received request to send email for 'Contest Order Approved' for Case ID: {}", caseId);
            if (notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
                && parties.contains(CaseRole.APP_SOLICITOR.getValue())) {
                log.info("Sending 'Contested Order Approved' email notification to Applicant Solicitor for Case ID: {}", caseId);
                notificationService.sendContestOrderApprovedEmailApplicant(caseDetails);
            }

            if (notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)
                && parties.contains(CaseRole.RESP_SOLICITOR.getValue())) {
                log.info("Sending 'Contested Order Approved' email notification to Respondent Solicitor for Case ID: {}", caseId);
                notificationService.sendContestOrderApprovedEmailRespondent(caseDetails);
            }
        }
    }
}
