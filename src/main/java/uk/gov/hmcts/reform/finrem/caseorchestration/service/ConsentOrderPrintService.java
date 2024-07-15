package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
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
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final InternationalPostalService postalService;

    public void sendConsentOrderToBulkPrint(FinremCaseDetails finremCaseDetails,
                                            FinremCaseDetails finremCaseDetailsBefore,
                                            EventType eventType,
                                            String authorisationToken) {
        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending approved order for applicant to bulk print for Case ID {}", finremCaseDetails.getId());
            UUID applicantLetterId = shouldPrintOrderApprovedDocuments(finremCaseDetails, authorisationToken)
                ? printApplicantConsentOrderApprovedDocuments(finremCaseDetails, finremCaseDetailsBefore, eventType, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(finremCaseDetails, authorisationToken);
            if (applicantLetterId != null) {
                finremCaseDetails.getData().setBulkPrintLetterIdApp(applicantLetterId.toString());
            }
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            log.info("Sending approved order for respondent to bulk print for Case ID: {}", finremCaseDetails.getId());
            generateCoversheetForRespondentAndSendOrders(finremCaseDetails, finremCaseDetailsBefore, eventType, authorisationToken);
        }
    }

    public FinremCaseDetails sendConsentOrderToBulkPrint(CaseDetails caseDetails,
                                                         CaseDetails caseDetailsBefore,
                                                         EventType eventType,
                                                         String authorisationToken) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        FinremCaseDetails finremCaseDetailsBefore = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetailsBefore);

        sendConsentOrderToBulkPrint(finremCaseDetails, finremCaseDetailsBefore, eventType, authorisationToken);

        return finremCaseDetails;
    }

    private void generateCoversheetForRespondentAndSendOrders(FinremCaseDetails finremCaseDetails,
                                                              FinremCaseDetails caseDetailsBefore,
                                                              EventType eventType,
                                                              String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(finremCaseDetails, authorisationToken);
        UUID respondentLetterId = sendConsentOrderForBulkPrintRespondent(respondentCoverSheet, finremCaseDetails,
            caseDetailsBefore, eventType, authorisationToken);
        FinremCaseData caseData = finremCaseDetails.getData();
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        if (caseDataService.isRespondentAddressConfidential(caseDetails.getData())) {
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
                                                             FinremCaseDetails caseDetailsBefore,
                                                             EventType eventType,
                                                             String authorisationToken) {
        List<BulkPrintDocument> bulkPrintDocuments = consentOrderApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        getOrderDocuments(caseDetails, caseDetailsBefore, eventType, authorisationToken, bulkPrintDocuments);
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, bulkPrintDocuments);
    }

    private UUID printApplicantConsentOrderNotApprovedDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);

        if (applicantDocuments.isEmpty()) {
            return null;
        }
        return bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, applicantDocuments);
    }

    private UUID sendConsentOrderForBulkPrintRespondent(CaseDocument coverSheet, FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore,
                                                        EventType eventType, String authorisationToken) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print, Case ID: {}", caseDetails.getId());
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(coverSheet));
        getOrderDocuments(caseDetails, caseDetailsBefore, eventType, authorisationToken, bulkPrintDocuments);
        DocumentHelper.PaperNotificationRecipient docRespondent;
        if (!shouldPrintOrderApprovedDocuments(caseDetails, authorisationToken)) {
            docRespondent = DocumentHelper.PaperNotificationRecipient.RESPONDENT;
            bulkPrintDocuments.add(consentOrderNotApprovedDocumentService.notApprovedCoverLetter(caseDetails, authorisationToken, docRespondent));
        }
        FinremCaseData caseData = caseDetails.getData();

        return bulkPrintService.bulkPrintFinancialRemedyLetterPack(
            caseDetails.getId(),
            RESPONDENT,
            bulkPrintDocuments,
            postalService.isRespondentResideOutsideOfUK(caseData),
            authorisationToken);
    }

    private void getOrderDocuments(FinremCaseDetails finremCaseDetails,
                                   FinremCaseDetails finremCaseDetailsBefore,
                                   EventType eventType,
                                   String authorisationToken,
                                   List<BulkPrintDocument> bulkPrintDocuments) {

        FinremCaseData caseData = finremCaseDetails.getData();
        List<CaseDocument> orderDocuments = caseDataService.isOrderApprovedCollectionPresent(caseData)
            ? consentOrderApprovedDocumentService.approvedOrderDocuments(finremCaseDetails, authorisationToken)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(finremCaseDetails);

        CaseDocument generalOrderBefore = finremCaseDetailsBefore.getData().getGeneralOrderWrapper().getGeneralOrderLatestDocument();
        CaseDocument generalOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();

        if (eventType.getCcdType().equals(EventType.APPROVE_ORDER.getCcdType())) {
            bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments));
        } else {
            if (!isNull(generalOrder) && isNull(generalOrderBefore)) {
                bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(generalOrder));
            } else if (isNull(generalOrder) && isNull(generalOrderBefore)) {
                bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments));
            } else if (!isNull(generalOrder) && !orderDocuments.isEmpty()
                && documentOrderingService.isDocumentModifiedLater(generalOrder, orderDocuments.get(0), authorisationToken)) {
                bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(generalOrder));
            } else if (!isNull(generalOrder) && orderDocuments.isEmpty()) {
                bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(generalOrder));
            } else {
                bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments));
            }
        }
    }


    public boolean shouldPrintOrderApprovedDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        boolean isOrderApprovedCollectionPresent = caseDataService.isOrderApprovedCollectionPresent(caseDetails.getData());
        boolean isOrderNotApprovedCollectionPresent = caseDataService.isContestedOrderNotApprovedCollectionPresent(caseDetails.getData());

        return isOrderApprovedCollectionPresent && (!isOrderNotApprovedCollectionPresent
            || documentOrderingService.isOrderApprovedCollectionModifiedLaterThanNotApprovedCollection(caseDetails, authorisationToken));
    }
}
