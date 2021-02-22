package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestedCaseOrderService {

    private final BulkPrintService bulkPrintService;
    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final PaperNotificationService paperNotificationService;
    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;

    public void printAndMailGeneralOrderToParties(CaseDetails caseDetails, String authorisationToken) {
        if (contestedGeneralOrderPresent(caseDetails)) {
            BulkPrintDocument generalOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData());

            if (paperNotificationService.shouldPrintForApplicant(caseDetails)) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }

            if (paperNotificationService.shouldPrintForRespondent(caseDetails)) {
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }
        }
    }

    public void printAndMailHearingDocuments(CaseDetails caseDetails, String authorisationToken) {
        if (caseDataService.isContestedPaperApplication(caseDetails)) {
            Map<String, Object> caseData = caseDetails.getData();

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

    private List<BulkPrintDocument> createHearingDocumentPack(Map<String, Object> caseData) {
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, CONTESTED_ORDER_APPROVED_COVER_LETTER).ifPresent(hearingDocumentPack::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, LATEST_DRAFT_HEARING_ORDER).ifPresent(hearingDocumentPack::add);

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(
                caseDocument -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument)));
        }

        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getCollectionOfDocumentLinksAsBulkPrintDocuments(
            caseData, HEARING_ORDER_OTHER_COLLECTION);

        if (otherHearingDocuments != null) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }

        return hearingDocumentPack;
    }

    private boolean contestedGeneralOrderPresent(CaseDetails caseDetails) {
        return !isNull(caseDetails.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
    }

    public void stampFinalOrder(CaseDetails caseDetails, String authToken) {
        Map<String, Object> caseData = caseDetails.getData();

        List<HearingOrderCollectionData> hearingOrderCollectionData = documentHelper.getHearingOrderDocuments(caseData);

        if (hearingOrderCollectionData != null && !hearingOrderCollectionData.isEmpty()) {
            CaseDocument latestHearingOrder = hearingOrderCollectionData
                .get(hearingOrderCollectionData.size() - 1)
                .getHearingOrderDocuments().getUploadDraftDocument();

            log.info("Received request to stampFinalOrder called with Case ID = {}, latestHearingOrder = {}", caseDetails.getId(),
                latestHearingOrder);

            stampAndAddToCollection(caseData, latestHearingOrder, authToken);
        }
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
