package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class SendOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final BulkPrintService bulkPrintService;
    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final NotificationService notificationService;
    private final DocumentHelper documentHelper;

    @Autowired
    public SendOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  BulkPrintService bulkPrintService, GeneralOrderService generalOrderService,
                                                  GenericDocumentService genericDocumentService, NotificationService notificationService,
                                                  DocumentHelper documentHelper) {
        super(finremCaseDetailsMapper);
        this.bulkPrintService = bulkPrintService;
        this.generalOrderService = generalOrderService;
        this.genericDocumentService = genericDocumentService;
        this.notificationService = notificationService;
        this.documentHelper = documentHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        try {
            printAndMailGeneralOrderToParties(caseDetails, userAuthorisation);
            printAndMailHearingDocuments(caseDetails, userAuthorisation);
            stampFinalOrder(caseDetails, userAuthorisation);
        } catch (InvalidCaseDataException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }

    private void stampFinalOrder(FinremCaseDetails caseDetails, String authToken) {

        List<DirectionOrderCollection> hearingOrderCollectionData = caseDetails.getData().getUploadHearingOrder();

        if (hearingOrderCollectionData != null && !hearingOrderCollectionData.isEmpty()) {
            int index = hearingOrderCollectionData.size() - 1;
            CaseDocument latestHearingOrder = hearingOrderCollectionData
                .get(index)
                .getValue().getUploadDraftDocument();

            List<DirectionOrderCollection> hearings = new ArrayList<>(hearingOrderCollectionData);
            String caseId = caseDetails.getId().toString();
            CaseDocument latestHearingOrderPdf =
                genericDocumentService.convertDocumentIfNotPdfAlready(latestHearingOrder, authToken, caseId);
            DirectionOrder document = DirectionOrder.builder().uploadDraftDocument(latestHearingOrderPdf).build();
            hearings.remove(index);
            hearings.add(index, DirectionOrderCollection.builder().value(document).build());
            caseDetails.getData().setUploadHearingOrder(hearings);

            log.info("Received request to stampFinalOrder called with Case ID = {},"
                    + " latestHearingOrder = {}, latestHearingOrderPdf {}", caseId,
                latestHearingOrder, latestHearingOrderPdf);

            stampAndAddToCollection(caseDetails.getData(), latestHearingOrderPdf, authToken, caseId);
        }
    }

    private void printAndMailGeneralOrderToParties(FinremCaseDetails caseDetails, String authorisationToken) {
        String caseId = String.valueOf(caseDetails.getId());
        log.info("In request to send general order for case {}:", caseId);
        if (contestedGeneralOrderPresent(caseDetails)) {
            log.info("General order found for case {}:", caseDetails.getId());
            BulkPrintDocument generalOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData(),
                authorisationToken, caseId);
            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Sending Applicant Order for Contested Case ID: {}", caseDetails.getId());
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }

            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Sending Respondent Order for Contested Case ID: {}", caseDetails.getId());
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }
        }
    }

    private void printAndMailHearingDocuments(FinremCaseDetails caseDetails, String authorisationToken) {

        String caseId = String.valueOf(caseDetails.getId());
        log.info("In request to send hearing pack for case {}:", caseId);

        List<BulkPrintDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails.getData(), authorisationToken, caseId);
        if (!hearingDocumentPack.isEmpty()) {
            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Received request to send hearing pack for applicant for case {}:", caseId);
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, hearingDocumentPack);
            }

            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Received request to send hearing pack for respondent for case {}:", caseId);
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, hearingDocumentPack);
            }
        }
    }

    private List<BulkPrintDocument> createHearingDocumentPack(FinremCaseData caseData, String authorisationToken, String caseId) {
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        Optional.ofNullable(documentHelper.getBulkPrintDocumentFromCaseDocument(caseData.getOrderApprovedCoverLetter())).ifPresent(hearingDocumentPack::add);
        Optional.ofNullable(documentHelper.getBulkPrintDocumentFromCaseDocument(caseData.getLatestDraftHearingOrder())).ifPresent(hearingDocumentPack::add);

        if (documentHelper.hasAnotherHearing(caseData)) {

            List<AdditionalHearingDocumentCollection> additionalHearingDocuments = caseData.getAdditionalHearingDocuments();

            if (additionalHearingDocuments != null && !additionalHearingDocuments.isEmpty()) {
                AdditionalHearingDocumentCollection additionalHearingDocumentCollection =
                    additionalHearingDocuments.get(additionalHearingDocuments.size() - 1);
                Optional.ofNullable(additionalHearingDocumentCollection.getValue().getDocument()).ifPresent(
                    caseDocument -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument)));
            }
        }
        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getHearingDocumentsAsBulkPrintDocuments(
            caseData, authorisationToken, caseId);

        if (otherHearingDocuments != null) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }

        return hearingDocumentPack;
    }

    private boolean contestedGeneralOrderPresent(FinremCaseDetails caseDetails) {
        return !isNull(caseDetails.getData().getGeneralOrderWrapper().getGeneralOrderLatestDocument());
    }

    private void stampAndAddToCollection(FinremCaseData caseData,
                                         CaseDocument latestHearingOrder,
                                         String authToken,
                                         String caseId) {
        if (!isEmpty(latestHearingOrder)) {
            StampType stampType = documentHelper.getStampType(caseData);
            CaseDocument stampedDocs =
                genericDocumentService.stampDocument(latestHearingOrder, authToken, stampType, caseId);
            log.info("Stamped Documents = {}", stampedDocs);

            List<DirectionOrderCollection> finalOrderCollection =
                Optional.ofNullable(caseData.getFinalOrderCollection())
                    .orElse(new ArrayList<>());
            log.info("Existing final order collection = {}", finalOrderCollection);

            finalOrderCollection.add(DirectionOrderCollection.builder()
                .value(DirectionOrder
                    .builder()
                    .uploadDraftDocument(stampedDocs)
                    .build())
                .build());
            log.info("Newly built final order collection = {}", finalOrderCollection);
            caseData.setFinalOrderCollection(finalOrderCollection);
            log.info("Finished stamping final order.");
        }
    }
}
