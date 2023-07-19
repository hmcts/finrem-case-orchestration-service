package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
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
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public void sendConsentOrderToBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending approved order for applicant to bulk print for case {}", finremCaseDetails.getId());
            UUID applicantLetterId = shouldPrintOrderApprovedDocuments(finremCaseDetails, authorisationToken)
                ? printApplicantConsentOrderApprovedDocuments(finremCaseDetails, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(finremCaseDetails, authorisationToken);
            finremCaseDetails.getData().setBulkPrintLetterIdApp(applicantLetterId.toString());
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Sending approved order for respondent to bulk print for case {}", caseDetails.getId());
            generateCoversheetForRespondentAndSendOrders(finremCaseDetails, authorisationToken);
        }
    }

    private void generateCoversheetForRespondentAndSendOrders(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(finremCaseDetails, authorisationToken);
        UUID respondentLetterId = sendConsentOrderForBulkPrintRespondent(respondentCoverSheet, finremCaseDetails, authorisationToken);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        if (caseDataService.isRespondentAddressConfidential(caseDetails.getData())) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
            finremCaseDetails.getData().setBulkPrintCoverSheetRes(null);
            finremCaseDetails.getData().setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            finremCaseDetails.getData().setBulkPrintCoverSheetRes(respondentCoverSheet);
            finremCaseDetails.getData().setBulkPrintLetterIdRes(respondentLetterId.toString());
        }

        log.info("Generated Respondent CoverSheet for bulk print, case {}. coversheet: {}, letterId : {}", caseDetails.getId(),
            respondentCoverSheet, respondentLetterId);
    }

    public UUID printApplicantConsentOrderApprovedDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    public UUID printApplicantConsentOrderNotApprovedDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);

        if (applicantDocuments.isEmpty()) {
            return null;
        }
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    private UUID sendConsentOrderForBulkPrintRespondent(CaseDocument coverSheet, FinremCaseDetails finremCaseDetails, String authorisationToken) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print, case {}", finremCaseDetails.getId());

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(coverSheet));

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        List<CaseDocument> orderDocuments = caseDataService.isOrderApprovedCollectionPresent(finremCaseDetails.getData())
            ? consentOrderApprovedDocumentService.approvedOrderDocuments(finremCaseDetails, authorisationToken)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails);

        if (!isNull(finremCaseDetails.getData().getGeneralOrderWrapper().getGeneralOrderLatestDocument())) {
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

    private UUID sendConsentInContestedOrderForBulkPrintRespondent(CaseDocument coverSheet, FinremCaseDetails finremCaseDetails, String authorisationToken) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print, case {}", finremCaseDetails.getId());

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(coverSheet));

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        List<CaseDocument> orderDocuments = caseDataService.isOrderApprovedCollectionPresent(finremCaseDetails.getData())
            ? consentOrderApprovedDocumentService.approvedOrderDocuments(finremCaseDetails, authorisationToken)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails);

        if (!isNull(finremCaseDetails.getData().getGeneralOrderWrapper().getGeneralOrderLatestDocument())) {
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
