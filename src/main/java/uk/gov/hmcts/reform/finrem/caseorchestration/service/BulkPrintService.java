package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPrintService {

    public static final String FINANCIAL_REMEDY_PACK_LETTER_TYPE = "FINANCIAL_REMEDY_PACK";
    private static final String FINANCIAL_REMEDY_GENERAL_LETTER = "FINREM002";

    private final String logMessage = "Case {}, has been marked as confidential. Adding coversheet to confidential field";
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final GenerateCoverSheetService coverSheetService;
    private final CaseDataService caseDataService;

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.

     * @return letterId to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    public UUID sendDocumentForPrint(final CaseDocument document, CaseDetails caseDetails, final String recipient) {
        List<BulkPrintDocument> bulkPrintDocument = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(document.getDocumentBinaryUrl())
                .fileName(document.getDocumentFilename())
                .build());

        return bulkPrintDocuments(caseDetails.getId(), FINANCIAL_REMEDY_GENERAL_LETTER, recipient, bulkPrintDocument);
    }

    public UUID sendDocumentForPrint(final CaseDocument document, FinremCaseDetails caseDetails, final String recipient) {
        List<BulkPrintDocument> bulkPrintDocument = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(document.getDocumentBinaryUrl())
                .fileName(document.getDocumentFilename()).build());

        return bulkPrintDocuments(caseDetails.getId(), FINANCIAL_REMEDY_GENERAL_LETTER, recipient, bulkPrintDocument);
    }

    public UUID bulkPrintFinancialRemedyLetterPack(Long caseId, String recipient, List<BulkPrintDocument> documents) {
        return bulkPrintDocuments(caseId, FINANCIAL_REMEDY_PACK_LETTER_TYPE, recipient, documents);
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.

     * @return letterId to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    public UUID printApplicantDocuments(CaseDetails caseDetails, String authorisationToken,
                                        List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateApplicantCoverSheet(caseDetails, authorisationToken),
            caseDocuments,
            APPLICANT);
    }

    public UUID printApplicantDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                        List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateApplicantCoverSheet(caseDetails, authorisationToken),
            caseDocuments,
            APPLICANT
            );
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.

     * @return letterId to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    public UUID printRespondentDocuments(CaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateRespondentCoverSheet(caseDetails, authorisationToken),
            caseDocuments,
            RESPONDENT);
    }

    public UUID printRespondentDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateRespondentCoverSheet(caseDetails, authorisationToken),
            caseDocuments,
            RESPONDENT);
    }

    private UUID bulkPrintDocuments(Long caseId, String letterType, String recipient, List<BulkPrintDocument> documents) {
        UUID letterId = genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(String.valueOf(caseId))
                .letterType(letterType)
                .bulkPrintDocuments(documents)
                .build(), recipient);

        log.info("Case {} Letter ID {} for {} document(s) of type {} sent to bulk print: {} and recipient is {}",
            caseId, letterId, documents.size(), letterType, documents, recipient);

        return letterId;
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.

     * @return sendLetterResponse response to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    private UUID printDocumentsWithCoversheet(CaseDetails caseDetails,
                                              BulkPrintDocument coverSheet,
                                              List<BulkPrintDocument> caseDocuments,
                                              String recipient) {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(coverSheet);
        documents.addAll(caseDocuments);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), recipient, documents);
    }

    private UUID printDocumentsWithCoversheet(FinremCaseDetails caseDetails,
                                              BulkPrintDocument coverSheet,
                                              List<BulkPrintDocument> caseDocuments,
                                              String recipient) {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(coverSheet);
        documents.addAll(caseDocuments);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), recipient, documents);
    }

    private BulkPrintDocument generateApplicantCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        CaseDocument applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
        log.info("Applicant cover sheet generated: Filename = {}", applicantCoverSheet.getDocumentFilename());

        if (YesOrNo.isYes(caseDetails.getData().getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent())) {
            log.info(logMessage, caseDetails.getId());
            caseDetails.getData().setBulkPrintCoverSheetApp(null);
            caseDetails.getData().setBulkPrintCoverSheetAppConfidential(applicantCoverSheet);
        } else {
            caseDetails.getData().setBulkPrintCoverSheetApp(applicantCoverSheet);
        }

        return documentHelper.getCaseDocumentAsBulkPrintDocument(applicantCoverSheet);
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.

     * @return BulkPrintDocument to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    private BulkPrintDocument generateApplicantCoverSheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);

        if (caseDataService.isApplicantAddressConfidential(caseDetails.getData())) {
            log.info(logMessage, caseDetails.getId());
            caseDetails.getData().remove(BULK_PRINT_COVER_SHEET_APP);
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL, applicantCoverSheet);
        } else {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP, applicantCoverSheet);
        }

        return documentHelper.getCaseDocumentAsBulkPrintDocument(applicantCoverSheet);
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.

     * @return BulkPrintDocument to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    private BulkPrintDocument generateRespondentCoverSheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);

        if (caseDataService.isRespondentAddressConfidential(caseDetails.getData())) {
            log.info(logMessage, caseDetails.getId());
            caseDetails.getData().remove(BULK_PRINT_COVER_SHEET_RES);
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL, respondentCoverSheet);
        } else {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_RES, respondentCoverSheet);
        }

        return documentHelper.getCaseDocumentAsBulkPrintDocument(respondentCoverSheet);
    }

    private BulkPrintDocument generateRespondentCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
        log.info("Respondent cover sheet generated: Filename = {}, url = {}, binUrl = {}",
            respondentCoverSheet.getDocumentFilename(), respondentCoverSheet.getDocumentUrl(), respondentCoverSheet.getDocumentBinaryUrl());

        if (YesOrNo.isYes(caseDetails.getData().getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant())) {
            log.info(logMessage, caseDetails.getId());
            caseDetails.getData().setBulkPrintCoverSheetRes(null);
            caseDetails.getData().setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseDetails.getData().setBulkPrintCoverSheetRes(respondentCoverSheet);
        }

        return documentHelper.getCaseDocumentAsBulkPrintDocument(respondentCoverSheet);
    }

    public String getRecipient(String text) {
        return StringUtils.remove(WordUtils.capitalizeFully(text, '_'), "_");
    }
}
