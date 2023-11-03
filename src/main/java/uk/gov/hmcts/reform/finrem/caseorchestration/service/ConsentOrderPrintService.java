package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
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
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public FinremCaseDetails sendConsentOrderToBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        FinremCaseDetails<FinremCaseDataConsented> finremCaseDetails =
            finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending approved order for applicant to bulk print for case {}", finremCaseDetails.getId());
            UUID applicantLetterId = shouldPrintOrderApprovedDocuments(finremCaseDetails, authorisationToken)
                ? printApplicantConsentOrderApprovedDocuments(finremCaseDetails, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(finremCaseDetails, authorisationToken);
            if (applicantLetterId != null) {
                finremCaseDetails.getData().setBulkPrintLetterIdApp(applicantLetterId.toString());
            }
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending approved order for respondent to bulk print for case {}", finremCaseDetails.getId());
            generateCoversheetForRespondentAndSendOrders(finremCaseDetails, authorisationToken);
        }

        return finremCaseDetails;
    }

    private void generateCoversheetForRespondentAndSendOrders(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(finremCaseDetails, authorisationToken);
        UUID respondentLetterId = sendConsentOrderForBulkPrintRespondent(respondentCoverSheet, finremCaseDetails, authorisationToken);
        FinremCaseData caseData = finremCaseDetails.getData();
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        if (caseDataService.isRespondentAddressConfidential(caseDetails.getData())) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", finremCaseDetails.getId());
            caseData.setBulkPrintCoverSheetRes(null);
            caseData.setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseData.setBulkPrintCoverSheetRes(respondentCoverSheet);
            caseData.setBulkPrintLetterIdRes(respondentLetterId.toString());
        }

        log.info("Generated Respondent CoverSheet for bulk print, case {}. coversheet: {}, letterId : {}", finremCaseDetails.getId(),
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

    private UUID sendConsentOrderForBulkPrintRespondent(CaseDocument coverSheet, FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print, case {}", caseDetails.getId());

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(coverSheet));

        FinremCaseData caseData = caseDetails.getData();

        List<CaseDocument> orderDocuments = caseDataService.isOrderApprovedCollectionPresent(caseData)
            ? consentOrderApprovedDocumentService.approvedOrderDocuments(caseDetails, authorisationToken)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails);

        if (!isNull(caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument())) {
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
