package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getLastMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderNotApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;
    private final GenerateCoverSheetService generateCoverSheetService;
    private final FeatureToggleService featureToggleService;
    private final GeneralOrderService generalOrderService;

    public List<BulkPrintDocument> prepareApplicantLetterPack(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        log.info("Generating consent order not approved documents for applicant, case ID {}", caseDetails.getId());

        List<BulkPrintDocument> documents = new ArrayList<>();

        if (featureToggleService.isConsentOrderNotApprovedApplicantDocumentGenerationEnabled()) {
            documents.add(coverLetter(caseDetails, authorisationToken));
            documents.addAll(notApprovedConsentOrder(caseData));
            documents = addGeneralOrdersIfApplicable(caseData, documents);
            documents.add(applicantReplyCoversheet(caseDetails, authorisationToken));

            //if only coversheet and reply sheet then print nothing
            if(documents.size() == 2) {
                return new ArrayList<>();
            }

        } else {
            documents.add(defaultCoversheet(caseDetails, authorisationToken));
            documents.addAll(notApprovedConsentOrder(caseData));
            documents = addGeneralOrdersIfApplicable(caseData, documents);

            //if only coversheet then print nothing
            if(documents.size() == 1) {
                return new ArrayList<>();
            }
        }

        return documents;
    }

    private List<BulkPrintDocument> addGeneralOrdersIfApplicable(Map<String, Object> caseData, List<BulkPrintDocument> existingList) {
        if (featureToggleService.isPrintGeneralOrderEnabled() && isPaperApplication(caseData)) {
            return Streams.concat(existingList.stream(),
                generalOrderService.getGeneralOrdersForPrintingConsented(caseData).stream()).collect(Collectors.toList());
        }
        return existingList;
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

    public List<BulkPrintDocument> notApprovedConsentOrder(Map<String, Object> caseData) {
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
                    .binaryFileUrl(documentLink.get(DOCUMENT_BINARY_URL).toString())
                    .build();
                log.info("Sending general order ({}) for bulk print.", documentLink.get(DOCUMENT_FILENAME));
                return asList(generalOrder);
            }
        }
        return Collections.emptyList();
    }

    private BulkPrintDocument applicantReplyCoversheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsWithTemplateData = documentHelper.prepareLetterToApplicantTemplateData(caseDetails);
        CaseDocument applicantCoversheet = genericDocumentService.generateDocument(
            authorisationToken,
            caseDetailsWithTemplateData,
            documentConfiguration.getConsentOrderNotApprovedReplyCoversheetTemplate(),
            documentConfiguration.getConsentOrderNotApprovedReplyCoversheetFileName());
        caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP, applicantCoversheet);
        return BulkPrintDocument.builder().binaryFileUrl(applicantCoversheet.getDocumentBinaryUrl()).build();
    }

    private BulkPrintDocument defaultCoversheet(CaseDetails caseDetails, String authorisationToken) {
        CaseDocument applicantCoverSheet = generateCoverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
        caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP, applicantCoverSheet);
        return BulkPrintDocument.builder().binaryFileUrl(applicantCoverSheet.getDocumentBinaryUrl()).build();
    }
}
