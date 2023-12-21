package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentOrderPrintService {

    private final BulkPrintService bulkPrintService;
    private final GenerateCoverSheetService coverSheetService;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    private final NotificationService notificationService;
    private final DocumentOrderingService documentOrderingService;
    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;

    public void sendConsentOrderToBulkPrint(FinremCaseDetails finremCaseDetails,
                                            String authorisationToken) {
        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending approved order for applicant to bulk print for Case ID {}", finremCaseDetails.getId());
            UUID applicantLetterId = shouldPrintOrderApprovedDocuments(finremCaseDetails, authorisationToken)
                ? printApplicantConsentOrderApprovedDocuments(finremCaseDetails, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(finremCaseDetails, authorisationToken);
            if (applicantLetterId != null) {
                finremCaseDetails.getData().setBulkPrintLetterIdApp(applicantLetterId.toString());
            }
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending approved order for respondent to bulk print for Case ID: {}", finremCaseDetails.getId());
            generateCoversheetForRespondentAndSendOrders(finremCaseDetails, authorisationToken);
        }
    }


    private void generateCoversheetForRespondentAndSendOrders(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(finremCaseDetails, authorisationToken);
        UUID respondentLetterId = sendConsentOrderForBulkPrintRespondent(respondentCoverSheet, finremCaseDetails, authorisationToken);
        FinremCaseData caseData = finremCaseDetails.getData();

        if (caseDataService.isRespondentAddressConfidential(caseData)) {
            log.info("Case ID: {}, has been marked as confidential. Adding coversheet to confidential field", finremCaseDetails.getId());
            caseData.setBulkPrintCoverSheetRes(null);
            caseData.setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseData.setBulkPrintCoverSheetRes(respondentCoverSheet);
            caseData.setBulkPrintLetterIdRes(respondentLetterId.toString());
        }

        log.info("Generated Respondent CoverSheet for bulk print, Case ID: {}. coversheet: {}, letterId : {}", finremCaseDetails.getId(),
            respondentCoverSheet, respondentLetterId);
    }

    private UUID printApplicantConsentOrderApprovedDocuments(FinremCaseDetails caseDetails,
                                                             String authorisationToken) {
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

    private UUID sendConsentOrderForBulkPrintRespondent(CaseDocument coverSheet, FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Preparing respondent order document for Bulk Print, Case ID: {}", caseDetails.getId());

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(coverSheet));

        FinremCaseData caseData = caseDetails.getData();
        boolean approvedCollectionPresent = caseDataService.isOrderApprovedCollectionPresent(caseData);
        List<CaseDocument> orderDocuments = approvedCollectionPresent
            ? consentOrderApprovedDocumentService.getApprovedOrderDocumentsAndSetAuditOrder(caseDetails, RESPONDENT, authorisationToken)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails);

        if (!isNull(caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument()) && !approvedCollectionPresent) {
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

        return bulkPrintService.bulkPrintFinancialRemedyLetterPack(
            caseDetails.getId(),
            RESPONDENT,
            bulkPrintDocuments,
            authorisationToken);
    }

    public boolean shouldPrintOrderApprovedDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        boolean isOrderApprovedCollectionPresent = caseDataService.isOrderApprovedCollectionPresent(caseDetails.getData());
        boolean isOrderNotApprovedCollectionPresent = caseDataService.isContestedOrderNotApprovedCollectionPresent(caseDetails.getData());

        return isOrderApprovedCollectionPresent && (!isOrderNotApprovedCollectionPresent
            || documentOrderingService.isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(caseDetails, authorisationToken));
    }
}
