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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
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
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final GenerateCoverSheetService coverSheetService;
    private final CaseDataService caseDataService;
    private final InternationalPostalService postalService;


    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     * <p>Use @link sendDocumentForPrint(final CaseDocument document, FinremCaseDetails caseDetails, final String recipient, String auth) instead </p>
     *
     * @return letterId to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    @SuppressWarnings("java:S1133")
    public UUID sendDocumentForPrint(final CaseDocument document,
                                     CaseDetails caseDetails,
                                     final String recipient,
                                     String auth) {

        List<BulkPrintDocument> bulkPrintDocument = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(document.getDocumentBinaryUrl())
                .fileName(document.getDocumentFilename())
                .build());
        return bulkPrintDocuments(caseDetails.getId(),
            FINANCIAL_REMEDY_GENERAL_LETTER,
            recipient,
            bulkPrintDocument,
            postalService.isRecipientResideOutsideOfUK(caseDetails.getData(), recipient),
            auth);
    }

    public UUID sendDocumentForPrint(final CaseDocument document, FinremCaseDetails caseDetails,
                                     final String recipient, String auth) {
        List<BulkPrintDocument> bulkPrintDocument = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(document.getDocumentBinaryUrl())
                .fileName(document.getDocumentFilename()).build());

        return bulkPrintDocuments(caseDetails.getId(), FINANCIAL_REMEDY_GENERAL_LETTER, recipient,
            bulkPrintDocument,
            postalService.isRecipientResideOutsideOfUK(caseDetails.getData(), recipient),
            auth);
    }

    public UUID bulkPrintFinancialRemedyLetterPack(Long caseId, String recipient, List<BulkPrintDocument> documents,
                                                   boolean isInternational, String auth) {
        log.info("Requesting {} letter print from bulkprint for Case ID: {}", recipient, caseId);
        return bulkPrintDocuments(caseId, FINANCIAL_REMEDY_PACK_LETTER_TYPE, recipient, documents, isInternational, auth);
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     *
     * @return letterId to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    @SuppressWarnings("java:S1133")
    public UUID printApplicantDocuments(CaseDetails caseDetails, String authorisationToken,
                                        List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateApplicantCoverSheet(caseDetails, authorisationToken), caseDocuments, APPLICANT,
            postalService.isApplicantResideOutsideOfUK(caseDetails.getData()),
            authorisationToken);

    }

    /**
     * Prints the applicant's documents with a coversheet.
     *
     * @param caseDetails          The case details.
     * @param authorisationToken   The authorisation token.
     * @param caseDocuments        The list of case documents to print.
     * @return The UUID of the printed documents.
     */
    public UUID printApplicantDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                        List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateApplicantCoverSheet(caseDetails, authorisationToken), caseDocuments, APPLICANT,
            postalService.isApplicantResideOutsideOfUK(caseDetails.getData()),
            authorisationToken);
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     *
     * @return letterId to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    @SuppressWarnings("java:S1133")
    public UUID printRespondentDocuments(CaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {

        return printDocumentsWithCoversheet(caseDetails,
            generateRespondentCoverSheet(caseDetails, authorisationToken),
            caseDocuments,
            RESPONDENT,
            postalService.isRespondentResideOutsideOfUK(caseDetails.getData()),
            authorisationToken);
    }

    public UUID printRespondentDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {

        return printDocumentsWithCoversheet(caseDetails,
            generateRespondentCoverSheet(caseDetails, authorisationToken), caseDocuments,
            RESPONDENT, postalService.isRespondentResideOutsideOfUK(caseDetails.getData()), authorisationToken);
    }

    private UUID bulkPrintDocuments(Long caseId, String letterType, String recipient, List<BulkPrintDocument> documents,
                                    boolean isInternational, String auth) {
        UUID letterId = genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(String.valueOf(caseId))
                .letterType(letterType)
                .bulkPrintDocuments(documents)
                .build(), recipient, isInternational, auth);

        log.info("Case {} Letter ID {} for {} document(s) of type {} sent to bulk print: {} and recipient is {}, isInternational {}",
            caseId, letterId, documents.size(), letterType, documents, recipient, isInternational);

        return letterId;
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     *
     * @return sendLetterResponse response to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    @SuppressWarnings("java:S1133")
    private UUID printDocumentsWithCoversheet(CaseDetails caseDetails,
                                              BulkPrintDocument coverSheet,
                                              List<BulkPrintDocument> caseDocuments,
                                              String recipient,
                                              boolean isInternational,
                                              String auth) {
        log.info("Adding all required document for bulkprint for Case ID: {}", caseDetails.getId());
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(coverSheet);
        documents.addAll(caseDocuments);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), recipient, documents, isInternational, auth);
    }

    private UUID printDocumentsWithCoversheet(FinremCaseDetails caseDetails,
                                              BulkPrintDocument coverSheet,
                                              List<BulkPrintDocument> caseDocuments,
                                              String recipient,
                                              boolean isInternational,
                                              String auth) {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(coverSheet);
        documents.addAll(caseDocuments);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), recipient, documents, isInternational, auth);
    }

    private BulkPrintDocument generateApplicantCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        CaseDocument applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
        log.info("Applicant coversheet generated {} for case Id {}", applicantCoverSheet, caseDetails.getId());

        if (YesOrNo.isYes(caseDetails.getData().getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent())) {
            log.info("Applicant has been marked as confidential, adding coversheet to confidential field for caseId {}", caseDetails.getId());
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(null);
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetAppConfidential(applicantCoverSheet);
        } else {
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(applicantCoverSheet);
        }

        return documentHelper.mapToBulkPrintDocument(applicantCoverSheet);
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     * <p>Use @link generateApplicantCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) instead </p>
     *
     * @return BulkPrintDocument to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    @SuppressWarnings("java:S1133")
    private BulkPrintDocument generateApplicantCoverSheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
        log.info("Applicant coversheet generated {} for case Id {}", applicantCoverSheet, caseDetails.getId());
        if (caseDataService.isApplicantAddressConfidential(caseDetails.getData())) {
            log.info("Applicant has been marked as confidential, adding coversheet to confidential field for caseId {}", caseDetails.getId());
            caseDetails.getData().remove(BULK_PRINT_COVER_SHEET_APP);
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL, applicantCoverSheet);
        } else {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP, applicantCoverSheet);
        }

        return documentHelper.mapToBulkPrintDocument(applicantCoverSheet);
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     * <p>Use @link generateRespondentCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) instead </p>
     * @return BulkPrintDocument to be return
     * @deprecated deprecated since 15-Feb-2023
     */
    @Deprecated(since = "15-Feb-2023")
    @SuppressWarnings("java:S1133")
    private BulkPrintDocument generateRespondentCoverSheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
        log.info("Respondent coversheet generated {} for case Id {}", respondentCoverSheet, caseDetails.getId());
        if (caseDataService.isRespondentAddressConfidential(caseDetails.getData())) {
            log.info("Respondent has been marked as confidential, adding coversheet to confidential field for caseId {}", caseDetails.getId());
            caseDetails.getData().remove(BULK_PRINT_COVER_SHEET_RES);
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL, respondentCoverSheet);
        } else {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_RES, respondentCoverSheet);
        }

        return documentHelper.mapToBulkPrintDocument(respondentCoverSheet);
    }

    private BulkPrintDocument generateRespondentCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
        log.info("Respondent cover sheet generated {}, for case Id {}", respondentCoverSheet, caseDetails.getId());

        if (YesOrNo.isYes(caseDetails.getData().getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant())) {
            log.info("Respondent has been marked as confidential, adding coversheet to confidential field for caseId {}", caseDetails.getId());
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(null);
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseDetails.getData().getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(respondentCoverSheet);
        }

        return documentHelper.mapToBulkPrintDocument(respondentCoverSheet);
    }

    private BulkPrintDocument generateIntervenerCoverSheet(FinremCaseDetails caseDetails, String authorisationToken,
                                                           DocumentHelper.PaperNotificationRecipient recipient) {
        CaseDocument intervenerCoverSheet =
            coverSheetService.generateIntervenerCoverSheet(caseDetails, authorisationToken, recipient);
        log.info("Intervener cover sheet generated {}, for case Id {}", intervenerCoverSheet, caseDetails.getId());
        return documentHelper.mapToBulkPrintDocument(intervenerCoverSheet);
    }


    /**
     * Return BulkPrintDocument.
     * <p>Please use @{@link #generateIntervenerCoverSheet(FinremCaseDetails, String, DocumentHelper.PaperNotificationRecipient)}</p>
     *
     * @param caseDetails        instance of CaseDetails
     * @param authorisationToken instance of String
     * @param recipient          instance of DocumentHelper.PaperNotificationRecipient
     * @deprecated Use {@link CaseDetails caseDetails, String authorisationToken,
     * DocumentHelper.PaperNotificationRecipient recipient}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    private BulkPrintDocument generateIntervenerCoverSheet(CaseDetails caseDetails, String authorisationToken,
                                                           DocumentHelper.PaperNotificationRecipient recipient) {
        CaseDocument intervenerCoverSheet = coverSheetService.generateIntervenerCoverSheet(caseDetails, authorisationToken, recipient);
        log.info("Intervener cover sheet generated {}, for case Id {}",
            intervenerCoverSheet, caseDetails.getId());
        return documentHelper.mapToBulkPrintDocument(intervenerCoverSheet);
    }

    public String getRecipient(String text) {
        return StringUtils.remove(WordUtils.capitalizeFully(text, '_'), "_");
    }


    /**
     * Return BulkPrintDocument.
     * <p>Please use @{@link #printIntervenerDocuments(IntervenerWrapper, FinremCaseDetails, String, List)}</p>
     *
     * @param intervenerWrapper  instance of IntervenerWrapper
     * @param caseDetails        instance of CaseDetails
     * @param authorisationToken instance of String
     * @param caseDocuments      instance of List
     * @deprecated Use {@link IntervenerWrapper intervenerWrapper, CaseDetails caseDetails,
     * String authorisationToken,
     * List caseDocuments}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    public UUID printIntervenerDocuments(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails,
                                         String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {

        return printDocumentsWithCoversheet(caseDetails,
            generateIntervenerCoverSheet(caseDetails, authorisationToken,
                intervenerWrapper.getPaperNotificationRecipient()), caseDocuments,
            intervenerWrapper.getIntervenerType().getTypeValue(),
            postalService.isIntervenerResideOutsideOfUK(intervenerWrapper),
            authorisationToken);
    }

    public UUID printIntervenerDocuments(IntervenerWrapper intervenerWrapper,
                                         FinremCaseDetails caseDetails,
                                         String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateIntervenerCoverSheet(caseDetails, authorisationToken, intervenerWrapper.getPaperNotificationRecipient()), caseDocuments,
            intervenerWrapper.getIntervenerType().getTypeValue(),
            postalService.isIntervenerResideOutsideOfUK(intervenerWrapper), authorisationToken);
    }
}

