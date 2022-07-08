package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved.ConsentOrderApprovedLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_PENSION_COLLECTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper mapper;
    private final ConsentOrderApprovedLetterDetailsMapper consentOrderApprovedLetterDetailsMapper;
    private final LetterDetailsMapper letterDetailsMapper;

    public Document generateApprovedConsentOrderLetter(FinremCaseDetails caseDetails, String authToken) {
        log.info("Generating Approved Consent Order Letter {} from {} for bulk print, case: {}",
            documentConfiguration.getApprovedConsentOrderFileName(),
            documentConfiguration.getApprovedConsentOrderTemplate(),
            caseDetails.getId());

        Map<String, Object> placeholdersMap = consentOrderApprovedLetterDetailsMapper
            .getConsentOrderApprovedLetterDetailsAsMap(caseDetails);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authToken, placeholdersMap,
            documentConfiguration.getApprovedConsentOrderTemplate(),
            documentConfiguration.getApprovedConsentOrderFileName());
    }

    public Document generateApprovedConsentOrderCoverLetter(FinremCaseDetails caseDetails, String authToken) {
        Map<String, Object> letterDetailsMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails, APPLICANT,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        Document generatedApprovedConsentOrderNotificationLetter = genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken, letterDetailsMap,
            documentConfiguration.getApprovedConsentOrderNotificationTemplate(),
            documentConfiguration.getApprovedConsentOrderNotificationFileName());

        log.info("Generated Approved Consent Order cover Letter: {}", generatedApprovedConsentOrderNotificationLetter);

        return generatedApprovedConsentOrderNotificationLetter;
    }

    public List<PensionCollectionData> stampPensionDocuments(List<PensionCollectionData> pensionList, String authToken) {
        return pensionList.stream()
            .filter(pensionCollectionData -> pensionCollectionData.getTypedCaseDocument().getPensionDocument() != null)
            .map(pensionCollectionData -> stampPensionDocuments(pensionCollectionData, authToken)).collect(toList());
    }

    private PensionCollectionData stampPensionDocuments(PensionCollectionData pensionDocument, String authToken) {
        CaseDocument document = pensionDocument.getTypedCaseDocument().getPensionDocument();
        CaseDocument stampedDocument = genericDocumentService.stampDocument(document, authToken);
        PensionCollectionData stampedPensionData = documentHelper.deepCopy(pensionDocument, PensionCollectionData.class);
        stampedPensionData.getTypedCaseDocument().setPensionDocument(stampedDocument);
        return stampedPensionData;
    }

    public List<BulkPrintDocument> prepareApplicantLetterPack(FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Sending Approved Consent Order to applicant / solicitor for Bulk Print, case {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getCaseData();

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        if (caseData.isPaperCase()) {
            Document coverLetter = generateApprovedConsentOrderCoverLetter(caseDetails, authorisationToken);
            bulkPrintDocuments.add(documentHelper.getDocumentAsBulkPrintDocument(coverLetter).orElse(null));
        }

        bulkPrintDocuments.addAll(documentHelper.getDocumentsAsBulkPrintDocuments(approvedOrderCollection(caseDetails)));

        return bulkPrintDocuments;
    }

    public void stampAndPopulateContestedConsentApprovedOrderCollection(Map<String, Object> caseData, String authToken) {
        CaseDocument stampedAndAnnexedDoc = stampAndAnnexContestedConsentOrder(caseData, authToken);
        List<PensionCollectionData> pensionDocs = consentInContestedStampPensionDocuments(caseData, authToken);
        populateContestedConsentOrderCaseDetails(caseData, stampedAndAnnexedDoc, pensionDocs);
    }

    public void generateAndPopulateConsentOrderLetter(FinremCaseDetails caseDetails, String authToken) {
        FinremCaseData caseData = caseDetails.getCaseData();
        Document orderLetter = generateApprovedConsentOrderLetter(caseDetails, authToken);
        List<ConsentOrderCollection> approvedOrders = caseData.getContestedConsentedApprovedOrders();
        if (approvedOrders != null && !approvedOrders.isEmpty()) {
            ConsentOrder approvedOrder = approvedOrders.get(approvedOrders.size() - 1).getValue();
            approvedOrder.setOrderLetter(orderLetter);
            caseData.setContestedConsentedApprovedOrders(approvedOrders);
        }
    }

    private CaseDocument stampAndAnnexContestedConsentOrder(Map<String, Object> caseData, String authToken) {
        CaseDocument latestConsentOrder = getLatestConsentInContestedConsentOrder(caseData);
        CaseDocument stampedDoc = genericDocumentService.stampDocument(latestConsentOrder, authToken);
        CaseDocument stampedAndAnnexedDoc = genericDocumentService.annexStampDocument(stampedDoc, authToken);
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

    private List<PensionCollectionData> consentInContestedStampPensionDocuments(Map<String, Object> caseData, String authToken) {
        List<PensionCollectionData> pensionDocs = getContestedConsentPensionDocuments(caseData);
        return stampPensionDocuments(pensionDocs, authToken);
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

    public List<Document> approvedOrderCollection(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<Document> documents = new ArrayList<>();
        List<ConsentOrderCollection> approvedOrders = getApprovedOrders(caseData);

        if (!approvedOrders.isEmpty()) {
            log.info("Extracting approved orders from case data for bulk print, case {}", caseDetails.getId());
            ConsentOrder lastApprovedOrder = approvedOrders.get(approvedOrders.size() - 1).getValue();
            Optional.ofNullable(lastApprovedOrder.getOrderLetter()).ifPresent(documents::add);
            Optional.ofNullable(lastApprovedOrder.getConsentOrder()).ifPresent(documents::add);
            documents.addAll(lastApprovedOrder.getPensionDocuments().stream()
                .map(pensionCollectionElement -> pensionCollectionElement.getValue().getUploadedDocument())
                .collect(toList()));
        } else {
            log.info("Failed to extract approved orders from case data for bulk print as document list was empty, case {}",
                caseDetails.getId());
        }
        return documents;
    }

    private List<ConsentOrderCollection> getApprovedOrders(FinremCaseData caseData) {
        return caseData.isConsentedInContestedCase()
            ? caseData.getContestedConsentedApprovedOrders()
            : caseData.getApprovedOrderCollection();
    }
}
