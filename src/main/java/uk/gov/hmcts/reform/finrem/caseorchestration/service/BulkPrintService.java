package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL;

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

    @Deprecated
    public UUID sendDocumentForPrint(final CaseDocument document, CaseDetails caseDetails, String auth) {
        List<BulkPrintDocument> bulkPrintDocument = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(document.getDocumentBinaryUrl())
                .fileName(document.getDocumentFilename())
                .build());

        return bulkPrintDocuments(caseDetails.getId(), FINANCIAL_REMEDY_GENERAL_LETTER, bulkPrintDocument, auth);
    }

    public UUID sendDocumentForPrint(final CaseDocument document, FinremCaseDetails caseDetails, String auth) {
        List<BulkPrintDocument> bulkPrintDocument = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(document.getDocumentBinaryUrl())
                .fileName(document.getDocumentFilename()).build());

        return bulkPrintDocuments(caseDetails.getId(), FINANCIAL_REMEDY_GENERAL_LETTER, bulkPrintDocument, auth);
    }

    public UUID bulkPrintFinancialRemedyLetterPack(Long caseId, List<BulkPrintDocument> documents, String auth) {
        return bulkPrintDocuments(caseId, FINANCIAL_REMEDY_PACK_LETTER_TYPE, documents, auth);
    }

    @Deprecated
    public UUID printApplicantDocuments(CaseDetails caseDetails, String authorisationToken,
                                        List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateApplicantCoverSheet(caseDetails, authorisationToken), caseDocuments, authorisationToken);
    }

    public UUID printApplicantDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                        List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateApplicantCoverSheet(caseDetails, authorisationToken), caseDocuments, authorisationToken);
    }

    @Deprecated
    public UUID printRespondentDocuments(CaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateRespondentCoverSheet(caseDetails, authorisationToken), caseDocuments, authorisationToken);
    }

    public UUID printRespondentDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails,
            generateRespondentCoverSheet(caseDetails, authorisationToken), caseDocuments, authorisationToken);
    }

    private UUID bulkPrintDocuments(Long caseId, String letterType, List<BulkPrintDocument> documents, String auth) {
        UUID letterId = genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(String.valueOf(caseId))
                .letterType(letterType)
                .bulkPrintDocuments(documents)
                .build(), auth);

        log.info("Case {} Letter ID {} for {} document(s) of type {} sent to bulk print: {}", caseId, letterId, documents.size(), letterType,
            documents);

        return letterId;
    }

    @Deprecated
    private UUID printDocumentsWithCoversheet(CaseDetails caseDetails,
                                              BulkPrintDocument coverSheet,
                                              List<BulkPrintDocument> caseDocuments,
                                              String auth) {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(coverSheet);
        documents.addAll(caseDocuments);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), documents, auth);
    }

    private UUID printDocumentsWithCoversheet(FinremCaseDetails caseDetails,
                                              BulkPrintDocument coverSheet,
                                              List<BulkPrintDocument> caseDocuments,
                                              String auth) {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(coverSheet);
        documents.addAll(caseDocuments);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), documents, auth);
    }

    private BulkPrintDocument generateApplicantCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        CaseDocument applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
        log.info("Applicant cover sheet generated: Filename = {}, url = {}, binUrl = {}",
            applicantCoverSheet.getDocumentFilename(), applicantCoverSheet.getDocumentUrl(), applicantCoverSheet.getDocumentBinaryUrl());

        if (YesOrNo.isYes(caseDetails.getData().getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent())) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
            caseDetails.getData().setBulkPrintCoverSheetApp(null);
            caseDetails.getData().setBulkPrintCoverSheetAppConfidential(applicantCoverSheet);
        } else {
            caseDetails.getData().setBulkPrintCoverSheetApp(applicantCoverSheet);
        }

        return documentHelper.getCaseDocumentAsBulkPrintDocument(applicantCoverSheet);
    }

    @Deprecated
    private BulkPrintDocument generateApplicantCoverSheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);

        if (caseDataService.isApplicantAddressConfidential(caseDetails.getData())) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
            caseDetails.getData().remove(BULK_PRINT_COVER_SHEET_APP);
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL, applicantCoverSheet);
        } else {
            caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP, applicantCoverSheet);
        }

        return documentHelper.getCaseDocumentAsBulkPrintDocument(applicantCoverSheet);
    }

    @Deprecated
    private BulkPrintDocument generateRespondentCoverSheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);

        if (caseDataService.isRespondentAddressConfidential(caseDetails.getData())) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
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
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
            caseDetails.getData().setBulkPrintCoverSheetRes(null);
            caseDetails.getData().setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseDetails.getData().setBulkPrintCoverSheetRes(respondentCoverSheet);
        }

        return documentHelper.getCaseDocumentAsBulkPrintDocument(respondentCoverSheet);
    }
}
