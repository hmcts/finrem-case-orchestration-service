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
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getFirstMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorAgreeToReceiveEmails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isOrderApprovedCollectionPresent;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@Service
@Slf4j
public class BulkPrintService {

    public static final String FINANCIAL_REMEDY_PACK_LETTER_TYPE = "FINANCIAL_REMEDY_PACK";
    private static final String FINANCIAL_REMEDY_GENERAL_LETTER = "FINREM002";
    static final String DOCUMENT_FILENAME = "document_filename";

    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final DocumentHelper documentHelper;
    private final GeneralOrderService generalOrderService;
    private final GenerateCoverSheetService coverSheetService;

    public BulkPrintService(GenericDocumentService genericDocumentService,
                            ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService,
                            @Lazy ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                            DocumentHelper documentHelper, GeneralOrderService generalOrderService,
                            GenerateCoverSheetService coverSheetService) {
        this.genericDocumentService = genericDocumentService;
        this.consentOrderNotApprovedDocumentService = consentOrderNotApprovedDocumentService;
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.documentHelper = documentHelper;
        this.generalOrderService = generalOrderService;
        this.coverSheetService = coverSheetService;
    }

    public UUID sendNotificationLetterForBulkPrint(final CaseDocument notificationLetter, final CaseDetails caseDetails) {
        List<BulkPrintDocument> notificationLetterList = Collections.singletonList(
            documentHelper.getCaseDocumentAsBulkPrintDocument(notificationLetter));

        Long caseId = caseDetails.getId();
        log.info("Notification letter sent to Bulk Print: {} for Case ID: {}", notificationLetterList, caseId);

        return bulkPrintFinancialRemedyLetterPack(caseId, notificationLetterList);
    }

    public UUID sendOrderForBulkPrintRespondent(final CaseDocument coverSheet, final CaseDetails caseDetails) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print");

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        Map<String, Object> caseData = caseDetails.getData();

        List<BulkPrintDocument> orderDocuments = isOrderApprovedCollectionPresent(caseData)
            ? approvedOrderCollection(caseDetails)
            : consentOrderNotApprovedDocumentService.notApprovedConsentOrder(caseDetails);

        bulkPrintDocuments.addAll(orderDocuments);

        if (!isOrderApprovedCollectionPresent(caseDetails.getData()) && !isNull(caseData.get(GENERAL_ORDER_LATEST_DOCUMENT))) {
            bulkPrintDocuments.add(generalOrderService.getLatestGeneralOrderForPrintingConsented(caseDetails.getData()));
        }

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

    public List<BulkPrintDocument> approvedOrderCollection(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List collection = CommonFunction.isConsentedInContestedCase(caseDetails)
            ? (List)data.get(CONTESTED_CONSENT_ORDER_COLLECTION)
            : (List)data.get(APPROVED_ORDER_COLLECTION);

        List<Map> documentList = ofNullable(collection)
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        if (!documentList.isEmpty()) {
            log.info("Extracting 'approvedOrderCollection' from case data for bulk print: {}", data);
            Map<String, Object> value = ((Map) getFirstMapValue.apply(documentList).get(VALUE));
            documentHelper.getDocumentLinkAsBulkPrintDocument(value, "orderLetter").ifPresent(bulkPrintDocuments::add);
            documentHelper.getDocumentLinkAsBulkPrintDocument(value, CONSENT_ORDER).ifPresent(bulkPrintDocuments::add);
            bulkPrintDocuments.addAll(documentHelper.getCollectionOfDocumentLinksAsBulkPrintDocuments(value,
                "pensionDocuments", "uploadedDocument"));
        } else {
            log.info("Failed to extract 'approvedOrderCollection' from case data for bulk print as document list was empty.");
        }

        return bulkPrintDocuments;
    }

    public UUID printApplicantConsentOrderNotApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken, generateApplicantCoverSheet(caseDetails, authorisationToken));

        if (applicantDocuments.isEmpty()) {
            return null;
        }
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), applicantDocuments);
    }

    private UUID printApplicantConsentOrderApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = new ArrayList<>();
        applicantDocuments.add(generateApplicantCoverSheet(caseDetails, authorisationToken));
        applicantDocuments.addAll(consentOrderApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken));
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

    public Map<String, Object> sendConsentOrderToBulkPrint(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();

        if (!isApplicantRepresentedByASolicitor(caseData) || !isApplicantSolicitorAgreeToReceiveEmails(caseData)
            || isPaperApplication(caseData)) {
            UUID applicantLetterId = isOrderApprovedCollectionPresent(caseData)
                ? printApplicantConsentOrderApprovedDocuments(caseDetails, authorisationToken)
                : printApplicantConsentOrderNotApprovedDocuments(caseDetails, authorisationToken);
            caseData.put(BULK_PRINT_LETTER_ID_APP, applicantLetterId);
        }

        generateCoversheetForRespondentAndSendOrders(caseDetails, authorisationToken);

        log.info("Bulk print is successful");

        return caseData;
    }

    public UUID printApplicantDocuments(CaseDetails caseDetails, String authorisationToken,
                                        List<BulkPrintDocument> caseDocuments) {
        return printDocuments(caseDetails, generateApplicantCoverSheet(caseDetails, authorisationToken), caseDocuments);
    }

    public UUID printRespondentDocuments(CaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {
        return printDocuments(caseDetails, generateRespondentCoverSheet(caseDetails, authorisationToken), caseDocuments);
    }

    private UUID printDocuments(CaseDetails caseDetails, BulkPrintDocument coverSheet,
                                List<BulkPrintDocument> caseDocuments) {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(coverSheet);
        documents.addAll(caseDocuments);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), documents);
    }

    private void generateCoversheetForRespondentAndSendOrders(CaseDetails caseDetails, String authToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authToken);
        UUID respondentLetterId = sendOrderForBulkPrintRespondent(respondentCoverSheet, caseDetails);

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(BULK_PRINT_COVER_SHEET_RES, respondentCoverSheet);
        caseData.put(BULK_PRINT_LETTER_ID_RES, respondentLetterId);

        log.info("Generated Respondent CoverSheet for bulk print. coversheet: {}, letterId : {}", respondentCoverSheet, respondentLetterId);
    }

    private BulkPrintDocument generateApplicantCoverSheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
        caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP, applicantCoverSheet);
        return documentHelper.getCaseDocumentAsBulkPrintDocument(applicantCoverSheet);
    }

    private BulkPrintDocument generateRespondentCoverSheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
        caseDetails.getData().put(BULK_PRINT_COVER_SHEET_RES, respondentCoverSheet);
        return documentHelper.getCaseDocumentAsBulkPrintDocument(respondentCoverSheet);
    }
}
