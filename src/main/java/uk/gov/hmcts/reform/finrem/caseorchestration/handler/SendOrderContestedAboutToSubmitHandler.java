package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.DocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendOrderContestedAboutToSubmitHandler implements CallbackHandler {

    private final BulkPrintService bulkPrintService;
    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final PaperNotificationService paperNotificationService;
    private final DocumentHelper documentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        printAndMailGeneralOrderToParties(caseDetails, userAuthorisation);
        printAndMailHearingDocuments(caseDetails, userAuthorisation);
        stampFinalOrder(caseDetails, userAuthorisation);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build();
    }

    private void stampFinalOrder(FinremCaseDetails caseDetails, String authToken) {
        FinremCaseData caseData = caseDetails.getCaseData();

        List<DirectionOrderCollection> hearingOrderCollectionData = caseData.getUploadHearingOrder();

        if (hearingOrderCollectionData != null && !hearingOrderCollectionData.isEmpty()) {
            Document latestHearingOrder = Iterables.getLast(hearingOrderCollectionData)
                .getValue().getUploadDraftDocument();

            log.info("Received request to stampFinalOrder called with Case ID = {}, latestHearingOrder = {}", caseDetails.getId(),
                latestHearingOrder);

            stampAndAddToCollection(caseData, latestHearingOrder, authToken);
        }
    }

    private void printAndMailGeneralOrderToParties(FinremCaseDetails caseDetails, String authorisationToken) {
        if (isContestedGeneralOrderPresent(caseDetails)) {
            BulkPrintDocument generalOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getCaseData());

            if (paperNotificationService.shouldPrintForApplicant(caseDetails)) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }

            if (paperNotificationService.shouldPrintForRespondent(caseDetails)) {
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }
        }
    }

    private void printAndMailHearingDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        if (caseDetails.getCaseData().isContestedPaperApplication()) {
            FinremCaseData caseData = caseDetails.getCaseData();

            List<BulkPrintDocument> hearingDocumentPack = createHearingDocumentPack(caseData);

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

    private List<BulkPrintDocument> createHearingDocumentPack(FinremCaseData caseData) {
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        documentHelper.getDocumentAsBulkPrintDocument(caseData.getOrderApprovedCoverLetter()).ifPresent(hearingDocumentPack::add);
        documentHelper.getDocumentAsBulkPrintDocument(caseData.getLatestDraftHearingOrder()).ifPresent(hearingDocumentPack::add);

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<Document> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(
                caseDocument ->
                    hearingDocumentPack.add(documentHelper.getDocumentAsBulkPrintDocument(caseDocument).orElse(null)));
        }

        List<Document> hearingOrderOtherDocs = Optional.ofNullable(caseData.getHearingOrderOtherDocuments())
            .orElse(new ArrayList<>()).stream()
            .map(DocumentCollection::getValue)
            .collect(Collectors.toList());

        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getDocumentsAsBulkPrintDocuments(hearingOrderOtherDocs);

        if (otherHearingDocuments != null) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }

        return hearingDocumentPack;
    }

    private boolean isContestedGeneralOrderPresent(FinremCaseDetails caseDetails) {
        return !isNull(caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderLatestDocument());
    }

    private void stampAndAddToCollection(FinremCaseData caseData, Document latestHearingOrder, String authToken) {
        if (!isEmpty(latestHearingOrder)) {
            Document stampedDocs = genericDocumentService.stampDocument(latestHearingOrder, authToken);
            log.info("Stamped Documents = {}", stampedDocs);
            List<DirectionOrderCollection> finalOrderCollection = Optional
                .ofNullable(caseData.getFinalOrderCollection()).orElse(new ArrayList<>());

            log.info("Existing final order collection = {}", finalOrderCollection);

            finalOrderCollection.add(DirectionOrderCollection.builder()
                    .value(DirectionOrder.builder()
                        .uploadDraftDocument(stampedDocs)
                        .build())
                .build());
            log.info("Newly built final order collection = {}", finalOrderCollection);
            caseData.setFinalOrderCollection(finalOrderCollection);
            log.info("Finished stamping final order.");
        }
    }
}
