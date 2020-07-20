package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getFirstMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorAgreeToReceiveEmails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isOrderApprovedDocumentCollectionPresent;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@Service
@Slf4j
public class BulkPrintService {

    private static final String FINANCIAL_REMEDY_PACK_LETTER_TYPE = "FINANCIAL_REMEDY_PACK";
    private static final String FINANCIAL_REMEDY_GENERAL_LETTER = "FINREM002";
    static final String DOCUMENT_FILENAME = "document_filename";

    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final DocumentHelper documentHelper;
    private final GenerateCoverSheetService coverSheetService;

    public BulkPrintService(GenericDocumentService genericDocumentService,
                            ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService,
                            @Lazy ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                            DocumentHelper documentHelper, GenerateCoverSheetService coverSheetService) {
        this.genericDocumentService = genericDocumentService;
        this.consentOrderNotApprovedDocumentService = consentOrderNotApprovedDocumentService;
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.documentHelper = documentHelper;
        this.coverSheetService = coverSheetService;
    }

    public UUID sendNotificationLetterForBulkPrint(final CaseDocument notificationLetter, final CaseDetails caseDetails) {
        List<BulkPrintDocument> notificationLetterList = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(notificationLetter.getDocumentBinaryUrl()).build());

        Long caseId = caseDetails.getId();
        log.info("Notification letter sent to Bulk Print: {} for Case ID: {}", notificationLetterList, caseId);

        return bulkPrintFinancialRemedyLetterPack(caseId, notificationLetterList);
    }

    private UUID sendOrderForBulkPrintRespondent(final CaseDocument coverSheet, final CaseDetails caseDetails) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print");

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        Map<String, Object> caseData = caseDetails.getData();
        List<BulkPrintDocument> orderDocuments = isOrderApprovedDocumentCollectionPresent(caseData)
            ? approvedOrderCollection(caseData)
            : asList(consentOrderNotApprovedDocumentService.generalOrder(caseData));

        bulkPrintDocuments.addAll(orderDocuments);

        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), bulkPrintDocuments);
    }

    public UUID printLatestGeneralLetter(CaseDetails caseDetails) {
        List<GeneralLetterData> generalLettersData = documentHelper.convertToGeneralLetterData(caseDetails.getData().get(GENERAL_LETTER));
        GeneralLetterData latestGeneralLetterData = generalLettersData.get(generalLettersData.size() - 1);
        BulkPrintDocument latestGeneralLetter = BulkPrintDocument.builder()
            .binaryFileUrl(latestGeneralLetterData.getGeneralLetter().getGeneratedLetter().getDocumentBinaryUrl())
            .build();
        return bulkPrintDocuments(caseDetails.getId(), FINANCIAL_REMEDY_GENERAL_LETTER, asList(latestGeneralLetter));
    }

    List<BulkPrintDocument> approvedOrderCollection(Map<String, Object> data) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get(APPROVED_ORDER_COLLECTION))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        if (!documentList.isEmpty()) {
            log.info("Extracting 'approvedOrderCollection' from case data for bulk print.");
            Map<String, Object> value = (Map<String, Object>) getFirstMapValue.apply(documentList).get(VALUE);
            documentHelper.getDocumentLinkAsBulkPrintDocument(value, "orderLetter").ifPresent(bulkPrintDocuments::add);
            documentHelper.getDocumentLinkAsBulkPrintDocument(value, CONSENT_ORDER).ifPresent(bulkPrintDocuments::add);
            bulkPrintDocuments.addAll(documentHelper.getCollectionOfDocumentLinksAsBulkPrintDocuments(value,
                "pensionDocuments", "uploadedDocument"));
        } else {
            log.info("Failed to extract 'approvedOrderCollection' from case data for bulk print as document list was empty.");
        }

        return bulkPrintDocuments;
    }

    private UUID printApplicantConsentOrderNotApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), applicantDocuments);
    }

    private UUID printApplicantConsentOrderApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), applicantDocuments);
    }

    private UUID bulkPrintFinancialRemedyLetterPack(Long caseId, List<BulkPrintDocument> documents) {
        return bulkPrintDocuments(caseId, FINANCIAL_REMEDY_PACK_LETTER_TYPE, documents);
    }

    private UUID bulkPrintDocuments(Long caseId, String letterType, List<BulkPrintDocument> documents) {
        UUID letterId = genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(String.valueOf(caseId))
                .letterType(letterType)
                .bulkPrintDocuments(documents)
                .build());

        log.info("Letter ID {} for {} document(s) of type {} sent to bulk print: {}", letterId, documents.size(), letterType, documents);

        return letterId;
    }

    public Map<String, Object> sendToBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();

        if (!isApplicantRepresentedByASolicitor(caseData) || isApplicantSolicitorAgreeToReceiveEmails(caseData)
            || isPaperApplication(caseData)) {
            UUID applicantLetterId = isOrderApprovedDocumentCollectionPresent(caseData)
                ? printApplicantConsentOrderApprovedDocuments(caseDetails, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(caseDetails, authorisationToken);
            caseData.put(BULK_PRINT_LETTER_ID_APP, applicantLetterId);
        }

        generateCoversheetForRespondentAndSendOrders(caseDetails, authorisationToken);

        log.info("Bulk print is successful");

        return caseData;
    }

    private void generateCoversheetForRespondentAndSendOrders(CaseDetails caseDetails, String authToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authToken);
        UUID respondentLetterId = sendOrderForBulkPrintRespondent(respondentCoverSheet, caseDetails);

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(BULK_PRINT_COVER_SHEET_RES, respondentCoverSheet);
        caseData.put(BULK_PRINT_LETTER_ID_RES, respondentLetterId);

        log.info("Generated Respondent CoverSheet for bulk print. coversheet: {}, letterId : {}", respondentCoverSheet, respondentLetterId);
    }
}
