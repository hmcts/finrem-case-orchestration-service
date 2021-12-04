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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_TITLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_PENSION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_TITLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper mapper;
    private final CaseDataService caseDataService;

    public CaseDocument generateApprovedConsentOrderLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Approved Consent Order Letter {} from {} for bulk print, case: {}",
            documentConfiguration.getApprovedConsentOrderFileName(),
            documentConfiguration.getApprovedConsentOrderTemplate(),
            caseDetails.getId());

        return genericDocumentService.generateDocument(authToken,
            caseDataService.isContestedApplication(caseDetails)
                ? prepareCaseDetailsCopyForDocumentGeneratorWithContestedFields(caseDetails)
                : caseDetails,
            documentConfiguration.getApprovedConsentOrderTemplate(),
            documentConfiguration.getApprovedConsentOrderFileName());
    }

    public CaseDocument generateApprovedConsentOrderCoverLetter(CaseDetails caseDetails, String authToken) {
        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, APPLICANT);

        CaseDocument generatedApprovedConsentOrderNotificationLetter = genericDocumentService.generateDocument(authToken, caseDetailsForBulkPrint,
            documentConfiguration.getApprovedConsentOrderNotificationTemplate(),
            documentConfiguration.getApprovedConsentOrderNotificationFileName());

        log.info("Generated Approved Consent Order cover Letter: {}", generatedApprovedConsentOrderNotificationLetter);

        return generatedApprovedConsentOrderNotificationLetter;
    }

    public List<PensionCollectionData> stampPensionDocuments(List<PensionCollectionData> pensionList, String authToken, String caseTypeId) {
        return pensionList.stream()
            .filter(pensionCollectionData -> pensionCollectionData.getTypedCaseDocument().getPensionDocument() != null)
            .map(pensionCollectionData -> stampPensionDocuments(pensionCollectionData, authToken, caseTypeId)).collect(toList());
    }

    private PensionCollectionData stampPensionDocuments(PensionCollectionData pensionDocument, String authToken, String caseTypeId) {
        CaseDocument document = pensionDocument.getTypedCaseDocument().getPensionDocument();
        CaseDocument stampedDocument = genericDocumentService.stampDocument(document, authToken, caseTypeId);
        PensionCollectionData stampedPensionData = documentHelper.deepCopy(pensionDocument, PensionCollectionData.class);
        stampedPensionData.getTypedCaseDocument().setPensionDocument(stampedDocument);

        return stampedPensionData;
    }

    public List<BulkPrintDocument> prepareApplicantLetterPack(CaseDetails caseDetails, String authorisationToken) {
        log.info("Sending Approved Consent Order to applicant / solicitor for Bulk Print, case {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        if (caseDataService.isPaperApplication(caseData)) {
            CaseDocument coverLetter = generateApprovedConsentOrderCoverLetter(caseDetails, authorisationToken);
            bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(coverLetter));
        }

        bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(approvedOrderCollection(caseDetails)));

        return bulkPrintDocuments;
    }

    public void stampAndPopulateContestedConsentApprovedOrderCollection(Map<String, Object> caseData, String authToken, String caseTypeId) {
        CaseDocument stampedAndAnnexedDoc = stampAndAnnexContestedConsentOrder(caseData, authToken, caseTypeId);
        List<PensionCollectionData> pensionDocs = consentInContestedStampPensionDocuments(caseData, authToken, caseTypeId);
        populateContestedConsentOrderCaseDetails(caseData, stampedAndAnnexedDoc, pensionDocs);
    }

    public void generateAndPopulateConsentOrderLetter(CaseDetails caseDetails, String authToken) {
        Map<String, Object> caseData = caseDetails.getData();
        CaseDocument orderLetter = generateApprovedConsentOrderLetter(caseDetails, authToken);
        List<CollectionElement<ApprovedOrder>> approvedOrders = getConsentInContestedApprovedOrderCollection(caseData);
        if (approvedOrders != null && !approvedOrders.isEmpty()) {
            ApprovedOrder approvedOrder = approvedOrders.get(approvedOrders.size() - 1).getValue();
            approvedOrder.setOrderLetter(orderLetter);
            caseData.put(CONTESTED_CONSENT_ORDER_COLLECTION, approvedOrders);
        }
    }

    private CaseDocument stampAndAnnexContestedConsentOrder(Map<String, Object> caseData, String authToken, String caseTypeId) {
        CaseDocument latestConsentOrder = getLatestConsentInContestedConsentOrder(caseData);
        CaseDocument stampedDoc = genericDocumentService.stampDocument(latestConsentOrder, authToken, caseTypeId);
        CaseDocument stampedAndAnnexedDoc = genericDocumentService.annexStampDocument(stampedDoc, authToken, caseTypeId);
        log.info("Stamped Document and Annex doc = {}", stampedAndAnnexedDoc);

        return stampedAndAnnexedDoc;
    }

    private void populateContestedConsentOrderCaseDetails(Map<String, Object> caseData, CaseDocument stampedDoc,
                                                          List<PensionCollectionData> pensionDocs) {
        caseData.put(CONSENT_ORDER, stampedDoc);
        caseData.put(CONTESTED_CONSENT_PENSION_COLLECTION, pensionDocs);

        List<CollectionElement<ApprovedOrder>> approvedOrders = Optional.ofNullable(getConsentInContestedApprovedOrderCollection(caseData))
            .orElse(new ArrayList<>());

        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .consentOrder(stampedDoc)
            .pensionDocuments(pensionDocs)
            .build();

        approvedOrders.add(CollectionElement.<ApprovedOrder>builder().value(approvedOrder).build());
        caseData.put(CONTESTED_CONSENT_ORDER_COLLECTION, approvedOrders);
    }

    private CaseDocument getLatestConsentInContestedConsentOrder(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(CONSENT_ORDER), new TypeReference<>() {});
    }

    private List<PensionCollectionData> consentInContestedStampPensionDocuments(Map<String, Object> caseData, String authToken,
                                                                                String caseTypeId) {
        List<PensionCollectionData> pensionDocs = getContestedConsentPensionDocuments(caseData);
        return stampPensionDocuments(pensionDocs, authToken, caseTypeId);
    }

    private List<PensionCollectionData> getContestedConsentPensionDocuments(Map<String, Object> caseData) {
        if (StringUtils.isEmpty(caseData.get(CONTESTED_CONSENT_PENSION_COLLECTION))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(CONTESTED_CONSENT_PENSION_COLLECTION), new TypeReference<>() {});
    }

    List<CollectionElement<ApprovedOrder>> getConsentInContestedApprovedOrderCollection(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(CONTESTED_CONSENT_ORDER_COLLECTION), new TypeReference<>() {});
    }

    private CaseDetails prepareCaseDetailsCopyForDocumentGeneratorWithContestedFields(CaseDetails caseDetails) {
        CaseDetails detailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = detailsCopy.getData();

        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, caseData.get(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME));
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, caseData.get(CONTESTED_RESPONDENT_LAST_NAME));

        caseData.put(CONSENTED_ORDER_DIRECTION_JUDGE_TITLE, caseData.get(CONTESTED_ORDER_DIRECTION_JUDGE_TITLE));
        caseData.put(CONSENTED_ORDER_DIRECTION_JUDGE_NAME, caseData.get(CONTESTED_ORDER_DIRECTION_JUDGE_NAME));
        caseData.put(CONSENTED_ORDER_DIRECTION_DATE, caseData.get(CONTESTED_ORDER_DIRECTION_DATE));

        return detailsCopy;
    }

    public List<CaseDocument> approvedOrderCollection(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        List<CaseDocument> documents = new ArrayList<>();
        String approvedOrderCollectionFieldName = caseDataService.isConsentedInContestedCase(caseDetails)
            ? CONTESTED_CONSENT_ORDER_COLLECTION : APPROVED_ORDER_COLLECTION;

        List<Map> approvedOrderCollectionData = ofNullable(data.get(approvedOrderCollectionFieldName))
            .map(List.class::cast)
            .orElse(new ArrayList<>());

        if (!approvedOrderCollectionData.isEmpty()) {
            log.info("Extracting '{}' from case data for bulk print, case {}", approvedOrderCollectionFieldName, caseDetails.getId());
            Map<String, Object> lastApprovedOrder = (Map<String, Object>)(approvedOrderCollectionData.get(approvedOrderCollectionData.size() - 1)
                .get(VALUE));
            documentHelper.getDocumentLinkAsCaseDocument(lastApprovedOrder, ORDER_LETTER).ifPresent(documents::add);
            documentHelper.getDocumentLinkAsCaseDocument(lastApprovedOrder, CONSENT_ORDER).ifPresent(documents::add);
            documents.addAll(documentHelper.getDocumentLinksFromCustomCollectionAsCaseDocuments(
                lastApprovedOrder,
                PENSION_DOCUMENTS,
                "uploadedDocument"));
        } else {
            log.info("Failed to extract '{}' from case data for bulk print as document list was empty, case {}",
                approvedOrderCollectionFieldName, caseDetails.getId());
        }

        return documents;
    }
}
