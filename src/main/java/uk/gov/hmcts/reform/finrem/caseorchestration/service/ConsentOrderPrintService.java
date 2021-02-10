package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentOrderPrintService {

    private final BulkPrintService bulkPrintService;
    private final GenerateCoverSheetService coverSheetService;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    private final PaperNotificationService paperNotificationService;
    private final DocumentOrderingService documentOrderingService;
    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;

    public void sendConsentOrderToBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();

        if (paperNotificationService.shouldPrintForApplicant(caseDetails)) {
            UUID applicantLetterId = shouldPrintOrderApprovedDocuments(caseDetails, authorisationToken)
                ? printApplicantConsentOrderApprovedDocuments(caseDetails, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(caseDetails, authorisationToken);
            caseData.put(BULK_PRINT_LETTER_ID_APP, applicantLetterId);
        }

        generateCoversheetForRespondentAndSendOrders(caseDetails, authorisationToken);

        log.info("Bulk print is successful");
    }

    private void generateCoversheetForRespondentAndSendOrders(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
        UUID respondentLetterId = sendConsentOrderForBulkPrintRespondent(respondentCoverSheet, caseDetails, authorisationToken);
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isRespondentAddressConfidential(caseData)) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
            caseData.remove(BULK_PRINT_COVER_SHEET_RES);
            caseData.put(BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL, respondentCoverSheet);
        } else {
            caseData.put(BULK_PRINT_COVER_SHEET_RES, respondentCoverSheet);
            caseData.put(BULK_PRINT_LETTER_ID_RES, respondentLetterId);
        }

        log.info("Generated Respondent CoverSheet for bulk print. coversheet: {}, letterId : {}", respondentCoverSheet, respondentLetterId);
    }

    private UUID printApplicantConsentOrderApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    private UUID printApplicantConsentOrderNotApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);

        if (applicantDocuments.isEmpty()) {
            return null;
        }
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    private UUID sendConsentOrderForBulkPrintRespondent(CaseDocument coverSheet, CaseDetails caseDetails, String authorisationToken) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print");

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(coverSheet));

        Map<String, Object> caseData = caseDetails.getData();

        List<CaseDocument> orderDocuments = caseDataService.isOrderApprovedCollectionPresent(caseData)
            ? consentOrderApprovedDocumentService.approvedOrderCollection(caseDetails)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails);

        if (!isNull(caseData.get(GENERAL_ORDER_LATEST_DOCUMENT))) {
            CaseDocument generalOrder = documentHelper.getLatestGeneralOrder(caseDetails.getData());

            if (orderDocuments.isEmpty()) {
                bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(generalOrder));
            } else {
                if (documentOrderingService.isDocumentModifiedLater(generalOrder, orderDocuments.get(0), authorisationToken)) {
                    bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(generalOrder));
                } else {
                    bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments));
                }
            }
        } else {
            bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments));
        }

        return bulkPrintService.bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), bulkPrintDocuments);
    }

    private boolean shouldPrintOrderApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        boolean isOrderApprovedCollectionPresent = caseDataService.isOrderApprovedCollectionPresent(caseDetails.getData());
        boolean isOrderNotApprovedCollectionPresent = caseDataService.isContestedOrderNotApprovedCollectionPresent(caseDetails.getData());

        return isOrderApprovedCollectionPresent && (!isOrderNotApprovedCollectionPresent
            || documentOrderingService.isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(caseDetails, authorisationToken));
    }
}
