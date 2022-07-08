package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

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
    private final DocumentHelper documentHelper;

    public void sendConsentOrderToBulkPrint(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getCaseData();

        if (paperNotificationService.shouldPrintForApplicant(caseDetails)) {
            UUID applicantLetterId = shouldPrintOrderApprovedDocuments(caseDetails, authorisationToken)
                ? printApplicantConsentOrderApprovedDocuments(caseDetails, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(caseDetails, authorisationToken);
            caseData.setBulkPrintLetterIdApp(String.valueOf(applicantLetterId));
        }

        if (paperNotificationService.shouldPrintForRespondent(caseDetails)) {
            generateCoversheetForRespondentAndSendOrders(caseDetails, authorisationToken);
        }

        log.info("Bulk print is successful, case {}", caseDetails.getId());
    }

    private void generateCoversheetForRespondentAndSendOrders(FinremCaseDetails caseDetails, String authorisationToken) {
        Document respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
        UUID respondentLetterId = sendConsentOrderForBulkPrintRespondent(respondentCoverSheet, caseDetails, authorisationToken);
        FinremCaseData caseData = caseDetails.getCaseData();

        if (caseData.getContactDetailsWrapper().getRespondentAddressConfidential().isYes()) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
            caseData.setBulkPrintCoverSheetRes(null);
            caseData.setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseData.setBulkPrintCoverSheetRes(respondentCoverSheet);
            caseData.setBulkPrintLetterIdRes(String.valueOf(respondentLetterId));
        }

        log.info("Generated Respondent CoverSheet for bulk print, case {}. coversheet: {}, letterId : {}", caseDetails.getId(),
            respondentCoverSheet, respondentLetterId);
    }

    private UUID printApplicantConsentOrderApprovedDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    private UUID printApplicantConsentOrderNotApprovedDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);

        if (applicantDocuments.isEmpty()) {
            return null;
        }
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    private UUID sendConsentOrderForBulkPrintRespondent(Document coverSheet, FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print, case {}", caseDetails.getId());

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(documentHelper.getDocumentAsBulkPrintDocument(coverSheet).orElse(null));

        FinremCaseData caseData = caseDetails.getCaseData();

        List<Document> orderDocuments = caseData.isOrderApprovedCollectionPresent()
            ? consentOrderApprovedDocumentService.approvedOrderCollection(caseDetails)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails);

        if (!isNull(caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument())) {
            Document generalOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();

            if (orderDocuments.isEmpty()) {
                bulkPrintDocuments.add(documentHelper.getDocumentAsBulkPrintDocument(generalOrder).get());
            } else {
                if (documentOrderingService.isDocumentModifiedLater(generalOrder, orderDocuments.get(0), authorisationToken)) {
                    bulkPrintDocuments.add(documentHelper.getDocumentAsBulkPrintDocument(generalOrder).get());
                } else {
                    bulkPrintDocuments.addAll(documentHelper.getDocumentsAsBulkPrintDocuments(orderDocuments));
                }
            }
        } else {
            bulkPrintDocuments.addAll(documentHelper.getDocumentsAsBulkPrintDocuments(orderDocuments));
        }

        return bulkPrintService.bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), bulkPrintDocuments);
    }

    private boolean shouldPrintOrderApprovedDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        boolean isOrderApprovedCollectionPresent = caseDetails.getCaseData().isOrderApprovedCollectionPresent();
        boolean isOrderNotApprovedCollectionPresent = caseDetails.getCaseData().isContestedOrderNotApprovedCollectionPresent();

        return isOrderApprovedCollectionPresent && (!isOrderNotApprovedCollectionPresent
            || documentOrderingService.isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(caseDetails, authorisationToken));
    }
}
