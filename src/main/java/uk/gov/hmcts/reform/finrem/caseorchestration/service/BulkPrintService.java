package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.isYes;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPrintService {

    public static final String FINANCIAL_REMEDY_PACK_LETTER_TYPE = "FINANCIAL_REMEDY_PACK";
    private static final String FINANCIAL_REMEDY_GENERAL_LETTER = "FINREM002";

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final GenerateCoverSheetService coverSheetService;

    @Deprecated
    public UUID sendDocumentForPrint(final CaseDocument document, CaseDetails caseDetails) {
        List<BulkPrintDocument> bulkPrintDocument = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(document.getDocumentBinaryUrl()).build());

        return bulkPrintDocuments(caseDetails.getId(), FINANCIAL_REMEDY_GENERAL_LETTER, bulkPrintDocument);
    }

    public UUID sendDocumentForPrint(final Document document, FinremCaseDetails caseDetails) {
        List<BulkPrintDocument> bulkPrintDocument = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(document.getBinaryUrl()).build());

        return bulkPrintDocuments(caseDetails.getId(), FINANCIAL_REMEDY_GENERAL_LETTER, bulkPrintDocument);
    }

    public UUID bulkPrintFinancialRemedyLetterPack(Long caseId, List<BulkPrintDocument> documents) {
        return bulkPrintDocuments(caseId, FINANCIAL_REMEDY_PACK_LETTER_TYPE, documents);
    }

    public UUID printApplicantDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                        List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails, generateApplicantCoverSheet(caseDetails, authorisationToken), caseDocuments);
    }

    public UUID printRespondentDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> caseDocuments) {
        return printDocumentsWithCoversheet(caseDetails, generateRespondentCoverSheet(caseDetails, authorisationToken), caseDocuments);
    }

    private UUID bulkPrintDocuments(Long caseId, String letterType, List<BulkPrintDocument> documents) {
        UUID letterId = genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(String.valueOf(caseId))
                .letterType(letterType)
                .bulkPrintDocuments(documents)
                .build());

        log.info("Case {} Letter ID {} for {} document(s) of type {} sent to bulk print: {}", caseId, letterId, documents.size(), letterType,
            documents);

        return letterId;
    }

    private UUID printDocumentsWithCoversheet(FinremCaseDetails caseDetails, BulkPrintDocument coverSheet, List<BulkPrintDocument> caseDocuments) {
        List<BulkPrintDocument> documents = new ArrayList<>();
        documents.add(coverSheet);
        documents.addAll(caseDocuments);
        return bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), documents);
    }

    private BulkPrintDocument generateApplicantCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        Document applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
        log.info("Applicant cover sheet generated: Filename = {}, url = {}, binUrl = {}",
            applicantCoverSheet.getFilename(), applicantCoverSheet.getUrl(), applicantCoverSheet.getBinaryUrl());

        if (isYes(caseDetails.getCaseData().getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent())) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
            caseDetails.getCaseData().setBulkPrintCoverSheetApp(null);
            caseDetails.getCaseData().setBulkPrintCoverSheetAppConfidential(applicantCoverSheet);
        } else {
            caseDetails.getCaseData().setBulkPrintCoverSheetApp(applicantCoverSheet);
        }

        return documentHelper.getDocumentAsBulkPrintDocument(applicantCoverSheet)
            .orElseThrow(IllegalArgumentException::new);
    }

    private BulkPrintDocument generateRespondentCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        Document respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
        log.info("Respondent cover sheet generated: Filename = {}, url = {}, binUrl = {}",
            respondentCoverSheet.getFilename(), respondentCoverSheet.getUrl(), respondentCoverSheet.getBinaryUrl());

        if (isYes(caseDetails.getCaseData().getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant())) {
            log.info("Case {}, has been marked as confidential. Adding coversheet to confidential field", caseDetails.getId());
            caseDetails.getCaseData().setBulkPrintCoverSheetRes(null);
            caseDetails.getCaseData().setBulkPrintCoverSheetResConfidential(respondentCoverSheet);
        } else {
            caseDetails.getCaseData().setBulkPrintCoverSheetRes(respondentCoverSheet);
        }

        return documentHelper.getDocumentAsBulkPrintDocument(respondentCoverSheet)
            .orElseThrow(IllegalArgumentException::new);
    }
}
