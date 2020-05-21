package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.DOCUMENT_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getLastMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;

    public List<BulkPrintDocument> generateApplicantDocuments(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();

        if (!isPaperApplication(caseData)) {
            return Collections.emptyList();
        }

        log.info("Generating consent order not approved documents for applicant, case ID {}", caseDetails.getId());

        List<BulkPrintDocument> documents = asList(
            coverLetter(caseDetails, authorisationToken),
            generalOrder(caseData),
            replyCoversheet(caseDetails, authorisationToken));

        return documents;
    }

    private BulkPrintDocument coverLetter(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsWithTemplateData = documentHelper.prepareLetterToApplicantTemplateData(caseDetails);
        CaseDocument coverLetter = genericDocumentService.generateDocument(
            authorisationToken,
            caseDetailsWithTemplateData,
            documentConfiguration.getConsentOrderNotApprovedCoverLetterTemplate(),
            documentConfiguration.getConsentOrderNotApprovedCoverLetterFileName());
        return BulkPrintDocument.builder().binaryFileUrl(coverLetter.getDocumentBinaryUrl()).build();
    }

    private BulkPrintDocument generalOrder(Map<String, Object> caseData) {
        log.info("Extracting 'uploadOrder' from case data for bulk print.");
        List<Map> documentList = ofNullable(caseData.get(UPLOAD_ORDER))
            .map(i -> (List<Map>) i)
            .orElse(Collections.emptyList());

        if (!documentList.isEmpty()) {
            Map<String, Object> value = ((Map) getLastMapValue.apply(documentList).get(VALUE));
            Object documentLinkObj = value.get("DocumentLink");
            if (documentLinkObj != null) {
                Map documentLink = (Map) documentLinkObj;
                BulkPrintDocument generalOrder = BulkPrintDocument.builder()
                    .binaryFileUrl(documentLink.get(DOCUMENT_URL).toString())
                    .build();
                log.info("Sending general order ({}) for bulk print.", documentLink.get(DOCUMENT_FILENAME));
                return generalOrder;
            }
        }

        throw new IllegalStateException("General order not found in application not approved case");
    }

    private BulkPrintDocument replyCoversheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsWithTemplateData = documentHelper.prepareLetterToApplicantTemplateData(caseDetails);
        CaseDocument coverLetter = genericDocumentService.generateDocument(
            authorisationToken,
            caseDetailsWithTemplateData,
            documentConfiguration.getConsentOrderNotApprovedReplyCoversheetTemplate(),
            documentConfiguration.getConsentOrderNotApprovedReplyCoversheetFileName());
        return BulkPrintDocument.builder().binaryFileUrl(coverLetter.getDocumentBinaryUrl()).build();
    }
}
