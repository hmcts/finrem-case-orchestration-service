package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendOrderContestedAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final BulkPrintService bulkPrintService;
    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final PaperNotificationService paperNotificationService;
    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        try {
            printAndMailGeneralOrderToParties(caseDetails, userAuthorisation);
            printAndMailHearingDocuments(caseDetails, userAuthorisation);
            stampFinalOrder(caseDetails, userAuthorisation);
        } catch (InvalidCaseDataException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }

    private void stampFinalOrder(CaseDetails caseDetails, String authToken) {
        Map<String, Object> caseData = caseDetails.getData();

        List<HearingOrderCollectionData> hearingOrderCollectionData = documentHelper.getHearingOrderDocuments(caseData);
        int index = hearingOrderCollectionData.size() - 1;
        if (hearingOrderCollectionData != null && !hearingOrderCollectionData.isEmpty()) {
            CaseDocument latestHearingOrder = hearingOrderCollectionData
                .get(index)
                .getHearingOrderDocuments().getUploadDraftDocument();

            List<HearingOrderCollectionData> hearings =  new ArrayList<>(hearingOrderCollectionData);
            CaseDocument latestHearingOrderPdf = genericDocumentService.convertDocumentIfNotPdfAlready(latestHearingOrder, authToken);
            HearingOrderDocument document = HearingOrderDocument.builder().uploadDraftDocument(latestHearingOrderPdf).build();
            hearings.remove(index);
            hearings.add(index, HearingOrderCollectionData.builder().hearingOrderDocuments(document).build());
            caseData.put(HEARING_ORDER_COLLECTION, hearings);

            log.info("Received request to stampFinalOrder called with Case ID = {},"
                    + " latestHearingOrder = {}, latestHearingOrderPdf {}", caseDetails.getId(),
                latestHearingOrder, latestHearingOrderPdf);

            stampAndAddToCollection(caseData, latestHearingOrderPdf, authToken);
        }
    }

    private void printAndMailGeneralOrderToParties(CaseDetails caseDetails, String authorisationToken) {
        if (contestedGeneralOrderPresent(caseDetails)) {
            BulkPrintDocument generalOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData(), authorisationToken);

            if (paperNotificationService.shouldPrintForApplicant(caseDetails)) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }

            if (paperNotificationService.shouldPrintForRespondent(caseDetails)) {
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }
        }
    }

    private void printAndMailHearingDocuments(CaseDetails caseDetails, String authorisationToken) {
        if (caseDataService.isContestedPaperApplication(caseDetails)) {
            Map<String, Object> caseData = caseDetails.getData();

            List<BulkPrintDocument> hearingDocumentPack = createHearingDocumentPack(caseData, authorisationToken);

            if (paperNotificationService.shouldPrintForApplicant(caseDetails)) {
                log.info("Received request to send hearing pack for applicant for case {}:", caseDetails.getId());
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, hearingDocumentPack);
            }

            if (paperNotificationService.shouldPrintForRespondent(caseDetails)) {
                log.info("Received request to send hearing pack for respondent for case {}:", caseDetails.getId());
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, hearingDocumentPack);
            }
        }
    }

    private List<BulkPrintDocument> createHearingDocumentPack(Map<String, Object> caseData, String authorisationToken) {
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, CONTESTED_ORDER_APPROVED_COVER_LETTER).ifPresent(hearingDocumentPack::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, LATEST_DRAFT_HEARING_ORDER).ifPresent(hearingDocumentPack::add);

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(
                caseDocument -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument)));
        }

        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getHearingDocumentsAsBulkPrintDocuments(
            caseData, authorisationToken);

        if (otherHearingDocuments != null) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }

        return hearingDocumentPack;
    }

    private boolean contestedGeneralOrderPresent(CaseDetails caseDetails) {
        return !isNull(caseDetails.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
    }

    private void stampAndAddToCollection(Map<String, Object> caseData, CaseDocument latestHearingOrder, String authToken) {
        if (!isEmpty(latestHearingOrder)) {
            CaseDocument stampedDocs = genericDocumentService.stampDocument(latestHearingOrder, authToken);
            log.info("Stamped Documents = {}", stampedDocs);

            List<HearingOrderCollectionData> finalOrderCollection = Optional.ofNullable(documentHelper.getFinalOrderDocuments(caseData))
                .orElse(new ArrayList<>());
            log.info("Existing final order collection = {}", finalOrderCollection);

            finalOrderCollection.add(HearingOrderCollectionData.builder()
                .hearingOrderDocuments(HearingOrderDocument
                    .builder()
                    .uploadDraftDocument(stampedDocs)
                    .build())
                .build());
            log.info("Newly built final order collection = {}", finalOrderCollection);
            caseData.put(FINAL_ORDER_COLLECTION, finalOrderCollection);
            log.info("Finished stamping final order.");
        }
    }
}
