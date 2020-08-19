package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.caseDocumentToBulkPrintDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_PENSION_COLLECTION;
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
    private final ObjectMapper mapper;

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

        List<BulkPrintDocument> approvedOrderCollection = bulkPrintService.approvedOrderCollection(caseDetails);
        bulkPrintDocuments.addAll(approvedOrderCollection);

        return bulkPrintDocuments;
    }

    public Map<String, Object> stampAndPopulateContestedConsentApprovedOrderCollection(Map<String, Object> caseData, String authToken) {
        CaseDocument stampedAndAnnexedDoc = stampAndAnnexContestedConsentOrder(caseData, authToken);
        List<PensionCollectionData> pensionDocs = consentInContestedStampPensionDocuments(caseData, authToken);
        caseData = populateContestedConsentOrderCaseDetails(caseData, stampedAndAnnexedDoc, pensionDocs);
        return caseData;
    }

    private CaseDocument stampAndAnnexContestedConsentOrder(Map<String, Object> caseData, String authToken) {
        CaseDocument latestConsentOrder = getLatestConsentInContestedConsentOrder(caseData);
        CaseDocument stampedDoc = genericDocumentService.stampDocument(latestConsentOrder, authToken);
        CaseDocument stampedAndAnnexedDoc = genericDocumentService.annexStampDocument(stampedDoc, authToken);
        log.info("Stamped Document and Annex doc = {}", stampedAndAnnexedDoc);
        return stampedAndAnnexedDoc;
    }

    private Map<String, Object> populateContestedConsentOrderCaseDetails(Map<String, Object> caseData,
                                                                         CaseDocument stampedDoc, List<PensionCollectionData> pensionDocs) {
        caseData.put(CONSENT_ORDER, stampedDoc);
        caseData.put(CONTESTED_CONSENT_PENSION_COLLECTION, pensionDocs);

        ApprovedOrder.ApprovedOrderBuilder approvedOrderBuilder = ApprovedOrder.builder()
            .consentOrder(stampedDoc)
            .pensionDocuments(pensionDocs);

        ApprovedOrderData.ApprovedOrderDataBuilder approvedOrderData = ApprovedOrderData.builder()
            .approvedOrder(approvedOrderBuilder.build());

        List<ApprovedOrderData> approvedOrderDataList = Optional.ofNullable(getConsentInContestedApprovedOrderCollection(caseData))
            .orElse(new ArrayList<>());
        approvedOrderDataList.add(approvedOrderData.build());
        caseData.put(CONTESTED_CONSENT_ORDER_COLLECTION, approvedOrderDataList);
        return caseData;
    }

    private CaseDocument getLatestConsentInContestedConsentOrder(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(CONSENT_ORDER),
            new TypeReference<CaseDocument>() {
            });
    }

    private List<PensionCollectionData> consentInContestedStampPensionDocuments(Map<String, Object> caseData, String authToken) {
        List<PensionCollectionData> pensionDocs = getContestedConsentPensionDocuments(caseData);
        return stampPensionDocuments(pensionDocs, authToken);
    }

    private List<PensionCollectionData> getContestedConsentPensionDocuments(Map<String, Object> caseData) {
        if (StringUtils.isEmpty(caseData.get(CONTESTED_CONSENT_PENSION_COLLECTION))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(CONTESTED_CONSENT_PENSION_COLLECTION),
            new TypeReference<List<PensionCollectionData>>() {
            });
    }

    private List<ApprovedOrderData> getConsentInContestedApprovedOrderCollection(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(CONTESTED_CONSENT_ORDER_COLLECTION), new TypeReference<List<ApprovedOrderData>>() {
        });
    }
}
