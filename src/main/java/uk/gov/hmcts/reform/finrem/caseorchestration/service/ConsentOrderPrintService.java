package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
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

    public void generateAndStoreCoverSheets(FinremCaseDetails caseDetails,
                                            String authorisationToken) {

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Generating and storing cover sheet for applicant for Case ID: {}", caseDetails.getId());
            coverSheetService.generateAndSetApplicantCoverSheet(caseDetails, authorisationToken);
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Generating and storing cover sheet for respondent for Case ID: {}", caseDetails.getId());
            coverSheetService.generateAndSetRespondentCoverSheet(caseDetails, authorisationToken);
        }
    }

    /**
     * Sends the consent order to bulk print using cover sheets that were already
     * generated and stored on the case. This is used in the submitted callback
     */
    public void sendConsentOrderToBulkPrintWithStoredCoverSheets(FinremCaseDetails caseDetails,
                                                                 FinremCaseDetails caseDetailsBefore,
                                                                 EventType eventType,
                                                                 String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Sending order to applicant for bulk print using stored cover sheet for Case ID: {}", caseDetails.getId());
            List<BulkPrintDocument> applicantDocuments =
                getApplicantOrderDocuments(caseDetails, caseDetailsBefore, eventType, authorisationToken);

            if (!applicantDocuments.isEmpty()) {
                CaseDocument coverSheet = getStoredCoverSheet(
                    caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp(),
                    caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential(),
                    "applicant", caseDetails.getId());

                List<BulkPrintDocument> documentsToPrint = new ArrayList<>();
                documentsToPrint.add(documentHelper.mapToBulkPrintDocument(coverSheet));
                documentsToPrint.addAll(applicantDocuments);

                bulkPrintService.bulkPrintFinancialRemedyLetterPack(caseDetails, CCDConfigConstant.APPLICANT,
                    documentsToPrint, postalService.isApplicantResideOutsideOfUK(caseData), authorisationToken);
            }
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Sending order to respondent for bulk print using stored cover sheet for Case ID: {}", caseDetails.getId());
            CaseDocument coverSheet = getStoredCoverSheet(
                caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes(),
                caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential(),
                "respondent", caseDetails.getId());

            sendConsentOrderForBulkPrintRespondent(coverSheet, caseDetails, caseDetailsBefore, eventType, authorisationToken);
        }
    }

    private List<BulkPrintDocument> getApplicantOrderDocuments(FinremCaseDetails caseDetails,
                                                               FinremCaseDetails caseDetailsBefore,
                                                               EventType eventType,
                                                               String authorisationToken) {
        if (shouldPrintOrderApprovedDocuments(caseDetails, authorisationToken)) {
            List<BulkPrintDocument> documents = consentOrderApprovedDocumentService.addApprovedConsentOrderCoverLetter(
                caseDetails, authorisationToken, APPLICANT);
            getOrderDocuments(caseDetails, caseDetailsBefore, eventType, authorisationToken, documents);
            return documents;
        }
        return consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(caseDetails, authorisationToken);
    }

    private CaseDocument getStoredCoverSheet(CaseDocument coverSheet, CaseDocument confidentialCoverSheet,
                                             String party, Long caseId) {
        return Optional.ofNullable(coverSheet)
            .or(() -> Optional.ofNullable(confidentialCoverSheet))
            .orElseThrow(() -> new IllegalStateException(
                "No stored %s cover sheet found for Case ID: %s".formatted(party, caseId)));
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
            caseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(null);
            caseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(respondentCoverSheet);
            caseData.setBulkPrintLetterIdRes(respondentLetterId.toString());
        }

        log.info("Generated Respondent CoverSheet for bulk print, Case ID: {}. coversheet: {}, letterId : {}", finremCaseDetails.getId(),
            respondentCoverSheet, respondentLetterId);
    }

    private UUID printApplicantConsentOrderApprovedDocuments(FinremCaseDetails caseDetails,
                                                             FinremCaseDetails caseDetailsBefore,
                                                             EventType eventType,
                                                             String authorisationToken) {
        List<BulkPrintDocument> bulkPrintDocuments = consentOrderApprovedDocumentService.addApprovedConsentOrderCoverLetter(
            caseDetails, authorisationToken, APPLICANT);
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
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(coverSheet));

        getOrderDocuments(caseDetails, caseDetailsBefore, eventType, authorisationToken, bulkPrintDocuments);
        DocumentHelper.PaperNotificationRecipient docRespondent = DocumentHelper.PaperNotificationRecipient.RESPONDENT;

        if (!shouldPrintOrderApprovedDocuments(caseDetails, authorisationToken)) {
            bulkPrintDocuments.add(consentOrderNotApprovedDocumentService.notApprovedCoverLetter(caseDetails, authorisationToken, docRespondent));
        } else {
            bulkPrintDocuments.addAll(consentOrderApprovedDocumentService.addApprovedConsentOrderCoverLetter(
                caseDetails, authorisationToken, docRespondent));
        }

        log.info("Sending order documents to respondent / solicitor for Bulk Print, Case ID: {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        return bulkPrintService.bulkPrintFinancialRemedyLetterPack(
            caseDetails,
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
            bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments,
                finremCaseDetails.getCaseType(), authorisationToken));
        } else {
            if (!isNull(generalOrder) && isNull(generalOrderBefore)) {
                bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(generalOrder));
            } else if (isNull(generalOrder) && isNull(generalOrderBefore)) {
                bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments,
                    finremCaseDetails.getCaseType(), authorisationToken));
            } else if (!isNull(generalOrder) && !orderDocuments.isEmpty()
                && documentOrderingService.isDocumentModifiedLater(generalOrder, orderDocuments.getFirst(), authorisationToken)) {
                bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(generalOrder));
            } else if (!isNull(generalOrder) && orderDocuments.isEmpty()) {
                bulkPrintDocuments.add(documentHelper.mapToBulkPrintDocument(generalOrder));
            } else {
                bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments,
                    finremCaseDetails.getCaseType(), authorisationToken));
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
