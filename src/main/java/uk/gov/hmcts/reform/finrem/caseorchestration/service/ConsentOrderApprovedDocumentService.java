package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved.ConsentOrderApprovedLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionTypeCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ConsentOrderApprovedLetterDetailsMapper consentOrderApprovedLetterDetailsMapper;
    private final LetterDetailsMapper letterDetailsMapper;
    private final ConsentedApplicationHelper consentedApplicationHelper;

    public Document generateApprovedConsentOrderLetter(FinremCaseDetails caseDetails, String authToken) {
        FinremCaseData caseData = caseDetails.getCaseData();
        String fileName = consentedApplicationHelper.getOrderApprovedFileName(caseData);

        log.info("Generating Approved {} Order Letter {} from {} for bulk print, case: {}",
            consentedApplicationHelper.getOrderType(caseData), fileName,
            documentConfiguration.getApprovedConsentOrderTemplate(),
            caseDetails.getId());

        Map<String, Object> placeholdersMap = consentOrderApprovedLetterDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authToken, placeholdersMap,
            documentConfiguration.getApprovedConsentOrderTemplate(),
            fileName);
    }

    public Document generateApprovedConsentOrderCoverLetter(FinremCaseDetails caseDetails, String authToken) {
        Map<String, Object> letterDetailsMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails, APPLICANT,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        String approvedOrderNotificationFileName =
            consentedApplicationHelper.getOrderApprovedNotificationFileName(caseDetails.getCaseData());

        Document generatedApprovedConsentOrderNotificationLetter = genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken, letterDetailsMap,
            documentConfiguration.getApprovedConsentOrderNotificationTemplate(),
            approvedOrderNotificationFileName);

        log.info("Generated Approved Consent Order cover Letter: {}", generatedApprovedConsentOrderNotificationLetter);

        return generatedApprovedConsentOrderNotificationLetter;
    }

    public List<PensionTypeCollection> stampPensionDocuments(List<PensionTypeCollection> pensionList, String authToken) {
        return pensionList.stream()
            .filter(pensionCollectionData -> pensionCollectionData.getValue().getUploadedDocument() != null)
            .map(pensionCollectionData -> stampPensionDocuments(pensionCollectionData, authToken)).collect(toList());
    }

    private PensionTypeCollection stampPensionDocuments(PensionTypeCollection pensionDocument, String authToken) {
        Document document = pensionDocument.getValue().getUploadedDocument();
        Document stampedDocument = genericDocumentService.stampDocument(document, authToken);
        PensionTypeCollection stampedPensionData = documentHelper.deepCopy(pensionDocument, PensionTypeCollection.class);
        stampedPensionData.getValue().setUploadedDocument(stampedDocument);
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

    public void stampAndPopulateContestedConsentApprovedOrderCollection(FinremCaseData caseData, String authToken) {
        Document stampedAndAnnexedDoc = stampAndAnnexContestedConsentOrder(caseData, authToken);
        List<PensionTypeCollection> pensionDocs = caseData.getConsentPensionCollection();
        populateContestedConsentOrderCaseDetails(caseData, stampedAndAnnexedDoc, pensionDocs);
    }

    public void generateAndPopulateConsentOrderLetter(FinremCaseDetails caseDetails, String authToken) {
        FinremCaseData caseData = caseDetails.getCaseData();
        Document orderLetter = generateApprovedConsentOrderLetter(caseDetails, authToken);
        List<ConsentOrderCollection> approvedOrders = caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders();
        if (!CollectionUtils.isEmpty(approvedOrders)) {
            ConsentOrder approvedOrder = Iterables.getLast(approvedOrders).getValue();
            approvedOrder.setOrderLetter(orderLetter);
            caseData.getConsentOrderWrapper().setContestedConsentedApprovedOrders(approvedOrders);
        }
    }

    private Document stampAndAnnexContestedConsentOrder(FinremCaseData caseData, String authToken) {
        Document latestConsentOrder = caseData.getConsentOrder();
        Document stampedDoc = genericDocumentService.stampDocument(latestConsentOrder, authToken);
        Document stampedAndAnnexedDoc = genericDocumentService.annexStampDocument(stampedDoc, authToken);
        log.info("Stamped Document and Annex doc = {}", stampedAndAnnexedDoc);
        return stampedAndAnnexedDoc;
    }

    private void populateContestedConsentOrderCaseDetails(FinremCaseData caseData, Document stampedDoc,
                                                          List<PensionTypeCollection> pensionDocs) {
        caseData.setConsentOrder(stampedDoc);
        caseData.setConsentPensionCollection(pensionDocs);

        List<ConsentOrderCollection> approvedOrders =
            Optional.ofNullable(caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders()).orElse(new ArrayList<>());

        ConsentOrderCollection approvedOrder = ConsentOrderCollection.builder()
            .value(ConsentOrder.builder()
                .consentOrder(stampedDoc)
                .pensionDocuments(pensionDocs)
                .build()).build();

        approvedOrders.add(approvedOrder);
        caseData.getConsentOrderWrapper().setContestedConsentedApprovedOrders(approvedOrders);
    }

    public List<Document> approvedOrderCollection(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<Document> documents = new ArrayList<>();
        List<ConsentOrderCollection> approvedOrders = getApprovedOrders(caseData);

        if (!CollectionUtils.isEmpty(approvedOrders)) {
            log.info("Extracting approved orders from case data for bulk print, case {}", caseDetails.getId());
            ConsentOrder lastApprovedOrder = Iterables.getLast(approvedOrders).getValue();
            Optional.ofNullable(lastApprovedOrder.getOrderLetter()).ifPresent(documents::add);
            Optional.ofNullable(lastApprovedOrder.getConsentOrder()).ifPresent(documents::add);
            documents.addAll(lastApprovedOrder.getPensionDocuments().stream()
                .map(pensionCollectionElement -> pensionCollectionElement.getValue().getUploadedDocument()).toList());
        } else {
            log.info("Failed to extract approved orders from case data for bulk print as document list was empty, case {}",
                caseDetails.getId());
        }
        return documents;
    }

    private List<ConsentOrderCollection> getApprovedOrders(FinremCaseData caseData) {
        return caseData.isConsentedInContestedCase()
            ? Optional.ofNullable(caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders()).orElse(new ArrayList<>())
            : Optional.ofNullable(caseData.getApprovedOrderCollection()).orElse(new ArrayList<>());
    }
}
