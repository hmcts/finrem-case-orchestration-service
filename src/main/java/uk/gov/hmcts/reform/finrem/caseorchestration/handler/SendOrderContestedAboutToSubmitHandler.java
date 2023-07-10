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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedSendOrderGeneralOrderLetterCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedSendOrderHearingLettersCorresponder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendOrderContestedAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final ContestedSendOrderGeneralOrderLetterCorresponder contestedSendOrderGeneralOrderLetterCorresponder;
    private final ContestedSendOrderHearingLettersCorresponder contestedSendOrderHearingLettersCorresponder;

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

        if (hearingOrderCollectionData != null && !hearingOrderCollectionData.isEmpty()) {
            int index = hearingOrderCollectionData.size() - 1;
            CaseDocument latestHearingOrder = hearingOrderCollectionData
                .get(index)
                .getHearingOrderDocuments().getUploadDraftDocument();

            List<HearingOrderCollectionData> hearings = new ArrayList<>(hearingOrderCollectionData);
            String caseId = caseDetails.getId().toString();
            CaseDocument latestHearingOrderPdf =
                genericDocumentService.convertDocumentIfNotPdfAlready(latestHearingOrder, authToken, caseId);
            HearingOrderDocument document = HearingOrderDocument.builder().uploadDraftDocument(latestHearingOrderPdf).build();
            hearings.remove(index);
            hearings.add(index, HearingOrderCollectionData.builder().hearingOrderDocuments(document).build());
            caseData.put(HEARING_ORDER_COLLECTION, hearings);

            log.info("Received request to stampFinalOrder called with Case ID = {},"
                    + " latestHearingOrder = {}, latestHearingOrderPdf {}", caseId,
                latestHearingOrder, latestHearingOrderPdf);

            stampAndAddToCollection(caseData, latestHearingOrderPdf, authToken, caseId);
        }
    }

    private void printAndMailGeneralOrderToParties(CaseDetails caseDetails, String authorisationToken) {
        String caseId = String.valueOf(caseDetails.getId());
        log.info("In request to send general order for case {}:", caseId);
        if (contestedGeneralOrderPresent(caseDetails)) {
            contestedSendOrderGeneralOrderLetterCorresponder.sendIntervenerCorrespondence(authorisationToken, caseDetails);
        }
    }

    private void printAndMailHearingDocuments(CaseDetails caseDetails, String authorisationToken) {

        String caseId = String.valueOf(caseDetails.getId());
        log.info("In request to send hearing pack for case {}:", caseId);
        contestedSendOrderHearingLettersCorresponder.sendCorrespondence(caseDetails, authorisationToken);

    }

    private boolean contestedGeneralOrderPresent(CaseDetails caseDetails) {
        return !isNull(caseDetails.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
    }

    private void stampAndAddToCollection(Map<String, Object> caseData,
                                         CaseDocument latestHearingOrder,
                                         String authToken,
                                         String caseId) {
        if (!isEmpty(latestHearingOrder)) {
            StampType stampType = documentHelper.getStampType(caseData);
            CaseDocument stampedDocs =
                genericDocumentService.stampDocument(latestHearingOrder, authToken, stampType, caseId);
            log.info("Stamped Documents = {}", stampedDocs);

            List<HearingOrderCollectionData> finalOrderCollection =
                Optional.ofNullable(documentHelper.getFinalOrderDocuments(caseData))
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
