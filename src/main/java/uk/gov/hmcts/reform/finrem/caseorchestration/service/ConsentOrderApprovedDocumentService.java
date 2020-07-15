package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.caseDocumentToBulkPrintDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final GenerateCoverSheetService generateCoverSheetService;
    private final DocumentConfiguration documentConfiguration;
    private final BulkPrintService bulkPrintService;
    private final DocumentHelper documentHelper;

    public CaseDocument generateApprovedConsentOrderLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Approved Consent Order Letter {} from {} for bulk print",
            documentConfiguration.getApprovedConsentOrderFileName(),
            documentConfiguration.getApprovedConsentOrderTemplate());

        return genericDocumentService.generateDocument(authToken, caseDetails,
            documentConfiguration.getApprovedConsentOrderTemplate(),
            documentConfiguration.getApprovedConsentOrderFileName());
    }

    public CaseDocument generateApprovedConsentOrderCoverLetter(CaseDetails caseDetails, String authToken) {
        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterToApplicantTemplateData(caseDetails);

        CaseDocument generatedApprovedConsentOrderNotificationLetter = genericDocumentService.generateDocument(authToken, caseDetailsForBulkPrint,
            documentConfiguration.getApprovedConsentOrderNotificationTemplate(),
            documentConfiguration.getApprovedConsentOrderNotificationFileName());

        log.info("Generated Approved Consent Order cover Letter: {}", generatedApprovedConsentOrderNotificationLetter);

        return generatedApprovedConsentOrderNotificationLetter;
    }

    public List<PensionCollectionData> stampPensionDocuments(List<PensionCollectionData> pensionList, String authToken) {
        return pensionList.stream()
                .map(data -> stampPensionDocuments(data, authToken)).collect(toList());
    }

    private PensionCollectionData stampPensionDocuments(PensionCollectionData pensionDocument, String authToken) {
        CaseDocument document = pensionDocument.getTypedCaseDocument().getPensionDocument();
        CaseDocument stampedDocument = genericDocumentService.stampDocument(document, authToken);
        PensionCollectionData stampedPensionData = documentHelper.deepCopy(pensionDocument, PensionCollectionData.class);
        stampedPensionData.getTypedCaseDocument().setPensionDocument(stampedDocument);
        return stampedPensionData;
    }

    public List<BulkPrintDocument> prepareApplicantLetterPack(CaseDetails caseDetails, String authorisationToken) {
        log.info("Sending Approved Consent Order to applicant / solicitor for Bulk Print");
        Map<String, Object> caseData = caseDetails.getData();
        CaseDocument applicantCoverSheet = generateCoverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
        caseData.put(BULK_PRINT_COVER_SHEET_APP, applicantCoverSheet);

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(caseDocumentToBulkPrintDocument(applicantCoverSheet));

        if (isPaperApplication(caseData)) {
            CaseDocument coverLetter = generateApprovedConsentOrderCoverLetter(caseDetails, authorisationToken);
            bulkPrintDocuments.add(caseDocumentToBulkPrintDocument(coverLetter));
        }

        List<BulkPrintDocument> approvedOrderCollection = bulkPrintService.approvedOrderCollection(caseDetails.getData());
        bulkPrintDocuments.addAll(approvedOrderCollection);

        return bulkPrintDocuments;
    }
}
