package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isOrderApprovedCollectionPresent;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentOrderPrintService {

    private final BulkPrintService bulkPrintService;
    private final GenerateCoverSheetService coverSheetService;
    private final GeneralOrderService generalOrderService;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    public void sendConsentOrderToBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();

        if (bulkPrintService.shouldPrintForApplicant(caseDetails)) {
            UUID applicantLetterId = isOrderApprovedCollectionPresent(caseData)
                ? printApplicantConsentOrderApprovedDocuments(caseDetails, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(caseDetails, authorisationToken);
            caseData.put(BULK_PRINT_LETTER_ID_APP, applicantLetterId);
        }

        generateCoversheetForRespondentAndSendOrders(caseDetails, authorisationToken);

        log.info("Bulk print is successful");
    }

    private void generateCoversheetForRespondentAndSendOrders(CaseDetails caseDetails, String authToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authToken);
        UUID respondentLetterId = sendConsentOrderForBulkPrintRespondent(respondentCoverSheet, caseDetails);

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(BULK_PRINT_COVER_SHEET_RES, respondentCoverSheet);
        caseData.put(BULK_PRINT_LETTER_ID_RES, respondentLetterId);

        log.info("Generated Respondent CoverSheet for bulk print. coversheet: {}, letterId : {}", respondentCoverSheet, respondentLetterId);
    }

    private UUID printApplicantConsentOrderApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    public UUID printApplicantConsentOrderNotApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);

        if (applicantDocuments.isEmpty()) {
            return null;
        }
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    public UUID sendConsentOrderForBulkPrintRespondent(final CaseDocument coverSheet, final CaseDetails caseDetails) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print");

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        Map<String, Object> caseData = caseDetails.getData();

        List<BulkPrintDocument> orderDocuments = isOrderApprovedCollectionPresent(caseData)
            ? consentOrderApprovedDocumentService.approvedOrderCollection(caseDetails)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails);

        bulkPrintDocuments.addAll(orderDocuments);

        if (!isOrderApprovedCollectionPresent(caseDetails.getData()) && !isNull(caseData.get(GENERAL_ORDER_LATEST_DOCUMENT))) {
            bulkPrintDocuments.add(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData()));
        }

        return bulkPrintService.bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), bulkPrintDocuments);
    }
}
