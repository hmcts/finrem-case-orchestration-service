package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint.BulkPrintCoverLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AuditSendOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AuditSendOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.ApprovedConsentOrderDocumentCategoriser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.VARIATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_TITLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_DIRECTION_JUDGE_TITLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentOrderingService documentOrderingService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper mapper;
    private final CaseDataService caseDataService;
    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final BulkPrintCoverLetterDetailsMapper bulkPrintLetterDetailsMapper;
    private final ApprovedConsentOrderDocumentCategoriser approvedConsentOrderCategoriser;

    public CaseDocument generateApprovedConsentOrderLetter(FinremCaseDetails finremCaseDetails, String authToken) {

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        String fileName;
        CaseDetails detailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = detailsCopy.getData();

        if (caseDataService.isConsentedApplication(caseDetails)
            && Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(caseDetails.getData()))) {
            fileName = documentConfiguration.getApprovedVariationOrderFileName();
            caseData.put(ORDER_TYPE, VARIATION);
        } else {
            fileName = documentConfiguration.getApprovedConsentOrderFileName();
            caseData.put(ORDER_TYPE, CONSENT);
        }

        log.info("Generating Approved {} Order Letter {} from {} for bulk print, Case ID: {}",
            caseData.get(ORDER_TYPE),
            fileName,
            documentConfiguration.getApprovedConsentOrderTemplate(caseDetails),
            detailsCopy.getId());

        return genericDocumentService.generateDocument(authToken,
            caseDataService.isContestedApplication(finremCaseDetails)
                ? prepareCaseDetailsCopyForDocumentGeneratorWithContestedFields(caseDetails)
                : detailsCopy,
            documentConfiguration.getApprovedConsentOrderTemplate(caseDetails),
            fileName);
    }

    public CaseDocument generateApprovedConsentOrderCoverLetter(FinremCaseDetails caseDetails,
                                                                String authToken) {
        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
        String approvedOrderNotificationFileName;
        if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(caseDetails.getData()))) {
            approvedOrderNotificationFileName
                = documentConfiguration.getApprovedVariationOrderNotificationFileName();
            caseDetailsForBulkPrint.getData().put(ORDER_TYPE, VARIATION);
        } else {
            approvedOrderNotificationFileName = documentConfiguration.getApprovedConsentOrderNotificationFileName();
            caseDetailsForBulkPrint.getData().put(ORDER_TYPE, CONSENT);
        }
        CaseDocument approvedLetter = genericDocumentService
            .generateDocument(authToken, caseDetailsForBulkPrint,
                documentConfiguration.getApprovedConsentOrderNotificationTemplate(),
                approvedOrderNotificationFileName);

        log.info("Generated Approved Consent Order cover Letter: {} for Case ID: {}",
            approvedLetter, caseDetails.getId());

        return approvedLetter;
    }

    public List<PensionTypeCollection> stampPensionDocuments(List<PensionTypeCollection> pensionList,
                                                             String authToken,
                                                             StampType stampType,
                                                             String caseId) {
        return pensionList.stream()
            .filter(pensionCollectionData -> pensionCollectionData.getTypedCaseDocument().getPensionDocument() != null)
            .map(pensionCollectionData -> stampPensionDocuments(pensionCollectionData, authToken, stampType, caseId))
            .toList();
    }

    private PensionTypeCollection stampPensionDocuments(PensionTypeCollection pensionDocument,
                                                        String authToken,
                                                        StampType stampType,
                                                        String caseId) {
        CaseDocument document = pensionDocument.getTypedCaseDocument().getPensionDocument();
        CaseDocument stampedDocument = genericDocumentService.stampDocument(document, authToken, stampType, caseId);
        PensionTypeCollection stampedPensionData = documentHelper.deepCopy(pensionDocument, PensionTypeCollection.class);
        stampedPensionData.getTypedCaseDocument().setPensionDocument(stampedDocument);
        return stampedPensionData;
    }

    public List<BulkPrintDocument> prepareApplicantLetterPack(FinremCaseDetails caseDetails,
                                                              String authorisationToken) {
        log.info("collecting Approved Consent Order to applicant / solicitor for Bulk Print, Case ID: {}", caseDetails.getId());
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        CaseDocument coverLetter = generateApprovedConsentOrderCoverLetter(caseDetails, authorisationToken);
        bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(coverLetter));

        List<CaseDocument> orderDocuments = getApprovedOrderDocumentsAndSetAuditOrder(caseDetails, APPLICANT, authorisationToken);

        bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(orderDocuments));
        return bulkPrintDocuments;
    }

    public void stampAndPopulateContestedConsentApprovedOrderCollection(FinremCaseData caseData, String authToken, String caseId) {
        CaseDocument stampedAndAnnexedDoc = stampAndAnnexContestedConsentOrder(caseData, authToken, caseId);
        List<PensionTypeCollection> pensionDocs = consentInContestedStampPensionDocuments(caseData, authToken, caseId);
        populateContestedConsentOrderCaseDetails(caseData, stampedAndAnnexedDoc, pensionDocs);
    }

    public void generateAndPopulateConsentOrderLetter(FinremCaseDetails caseDetails, String authToken) {
        FinremCaseData caseData = caseDetails.getData();
        CaseDocument orderLetter = generateApprovedConsentOrderLetter(caseDetails, authToken);
        ConsentOrderWrapper consentOrderWrapper = caseData.getConsentOrderWrapper();
        List<ConsentOrderCollection> approvedOrders = consentOrderWrapper.getContestedConsentedApprovedOrders();

        if (approvedOrders != null && !approvedOrders.isEmpty()) {
            ApprovedOrder approvedOrder = approvedOrders.get(approvedOrders.size() - 1).getApprovedOrder();
            approvedOrder.setOrderLetter(orderLetter);
            consentOrderWrapper.setContestedConsentedApprovedOrders(approvedOrders);
        }
        approvedConsentOrderCategoriser.categorise(caseDetails.getData());
    }

    private CaseDocument stampAndAnnexContestedConsentOrder(FinremCaseData caseData,
                                                            String authToken,
                                                            String caseId) {
        CaseDocument latestConsentOrder = caseData.getConsentOrder();
        CaseDocument pdfDocument =
            genericDocumentService.convertDocumentIfNotPdfAlready(latestConsentOrder, authToken, caseId);
        caseData.setConsentOrder(pdfDocument);
        StampType stampType = documentHelper.getStampType(caseData);
        CaseDocument stampedDoc = genericDocumentService.stampDocument(pdfDocument, authToken, stampType, caseId);
        CaseDocument stampedAndAnnexedDoc =
            genericDocumentService.annexStampDocument(stampedDoc, authToken, stampType, caseId);
        log.info("Stamped Document and Annex doc = {} for Case ID: {}", stampedAndAnnexedDoc, caseId);
        return stampedAndAnnexedDoc;
    }

    private void populateContestedConsentOrderCaseDetails(FinremCaseData caseData, CaseDocument stampedDoc,
                                                          List<PensionTypeCollection> pensionDocs) {
        caseData.setConsentOrder(stampedDoc);
        caseData.setConsentPensionCollection(pensionDocs);
        ConsentOrderWrapper consentOrderWrapper = caseData.getConsentOrderWrapper();
        List<ConsentOrderCollection> approvedOrders = Optional.ofNullable(consentOrderWrapper
                .getContestedConsentedApprovedOrders()).orElse(new ArrayList<>());


        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .consentOrder(stampedDoc)
            .pensionDocuments(pensionDocs)
            .build();

        ConsentOrderCollection orderCollection = ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();

        approvedOrders.add(orderCollection);
        consentOrderWrapper.setContestedConsentedApprovedOrders(approvedOrders);
    }


    private List<PensionTypeCollection> consentInContestedStampPensionDocuments(FinremCaseData caseData,
                                                                                String authToken,
                                                                                String caseId) {
        List<PensionTypeCollection> pensionDocs = Optional.ofNullable(caseData.getConsentPensionCollection())
            .orElse(new ArrayList<>());
        StampType stampType = documentHelper.getStampType(caseData);
        return stampPensionDocuments(pensionDocs, authToken, stampType, caseId);
    }


    List<ConsentOrderCollection> getConsentInContestedApprovedOrderCollection(FinremCaseData caseData) {
        return caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders();
    }

    public CaseDetails prepareCaseDetailsCopyForDocumentGeneratorWithContestedFields(CaseDetails caseDetails) {

        CaseDetails detailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = detailsCopy.getData();

        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, caseData.get(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME));
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, caseData.get(CONTESTED_RESPONDENT_LAST_NAME));

        caseData.put(CONSENTED_ORDER_DIRECTION_JUDGE_TITLE, caseData.get(CONTESTED_ORDER_DIRECTION_JUDGE_TITLE));
        caseData.put(CONSENTED_ORDER_DIRECTION_JUDGE_NAME, caseData.get(CONTESTED_ORDER_DIRECTION_JUDGE_NAME));
        caseData.put(CONSENTED_ORDER_DIRECTION_DATE, caseData.get(CONTESTED_ORDER_DIRECTION_DATE));

        return detailsCopy;
    }

    public List<CaseDocument> getApprovedOrderDocumentsAndSetAuditOrder(FinremCaseDetails finremCaseDetails,
                                                                        String party,
                                                                        String authorisationToken) {
        FinremCaseData data = finremCaseDetails.getData();
        CaseDocument generalOrderLatestDocument = data.getGeneralOrderWrapper().getGeneralOrderLatestDocument();
        List<ConsentOrderCollection> orderCollection;
        ConsentOrderWrapper orderWrapper = data.getConsentOrderWrapper();

        if (!caseDataService.isConsentedInContestedCase(finremCaseDetails)) {
            orderCollection = data.getApprovedOrderCollection();
        } else {
            orderCollection = orderWrapper.getContestedConsentedApprovedOrders();
        }

        List<AuditSendOrderCollection> auditSendOrders = Optional.ofNullable(orderWrapper.getAuditSendOrders())
            .orElse(new ArrayList<>());

        boolean generalOrderRecentThanApprovedOrder
            = documentOrderingService.isGeneralOrderRecentThanApprovedOrder(generalOrderLatestDocument,
            orderCollection, authorisationToken);

        List<CaseDocument> documents = new ArrayList<>();

        if (generalOrderLatestDocument != null && !orderCollection.isEmpty() && generalOrderRecentThanApprovedOrder) {
            if (isOrderSentOrAlreadyInAuditCollection(generalOrderLatestDocument, auditSendOrders, party)) {
                documents.add(generalOrderLatestDocument);
                AuditSendOrder sendOrder = getAuditObj(generalOrderLatestDocument, party);
                auditSendOrders.add(AuditSendOrderCollection.builder().value(sendOrder).build());
                orderWrapper.setAuditSendOrders(auditSendOrders);
            } else {
                log.info("General Order has been sent already for case {}", finremCaseDetails.getId());
                getOrderDocuments(finremCaseDetails, authorisationToken, orderCollection, orderWrapper, auditSendOrders, documents, party);
            }
        } else if (generalOrderLatestDocument == null || !orderCollection.isEmpty()) {
            getOrderDocuments(finremCaseDetails, authorisationToken, orderCollection, orderWrapper, auditSendOrders, documents, party);
        }
        return documents;
    }

    private void getOrderDocuments(FinremCaseDetails finremCaseDetails, String authorisationToken,
                                   List<ConsentOrderCollection> orderCollection,
                                   ConsentOrderWrapper orderWrapper,
                                   List<AuditSendOrderCollection> auditSendOrders,
                                   List<CaseDocument> documents,
                                   String party) {
        orderCollection.forEach(order -> {
            if (isOrderSentOrAlreadyInAuditCollection(order.getApprovedOrder().getConsentOrder(), auditSendOrders, party)) {
                AuditSendOrder sendOrder = getAuditObj(order.getApprovedOrder().getConsentOrder(), party);
                auditSendOrders.add(AuditSendOrderCollection.builder().value(sendOrder).build());
                orderWrapper.setAuditSendOrders(auditSendOrders);
                bulkPrintDocuments(order, documents,
                    authorisationToken, String.valueOf(finremCaseDetails.getId()));
            }
        });
    }

    private AuditSendOrder getAuditObj(CaseDocument caseDocument, String party) {
        return AuditSendOrder.builder()
            .orderName(caseDocument.getDocumentFilename())
            .orderDateTime(LocalDateTime.now())
            .documentId(getDocumentId(caseDocument))
            .party(party)
            .build();
    }

    private boolean isOrderSentOrAlreadyInAuditCollection(CaseDocument caseDocument,
                                                          List<AuditSendOrderCollection> auditSendOrders,
                                                          String party) {

        if (!auditSendOrders.isEmpty()) {
            for (AuditSendOrderCollection order : auditSendOrders) {
                if (caseDocument
                    .getDocumentFilename().equals(order.getValue().getOrderName())
                    && getDocumentId(caseDocument).equals(order.getValue().getDocumentId())
                    && order.getValue().getParty().equals(party)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getDocumentId(CaseDocument caseDocument) {
        String documentUrl = caseDocument.getDocumentUrl();
        return documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
    }

    private void bulkPrintDocuments(ConsentOrderCollection order,
                                    List<CaseDocument> documents,
                                    String authorisationToken,
                                    String caseId) {
        CaseDocument consentOrder = order.getApprovedOrder().getConsentOrder();
        if (consentOrder != null) {
            CaseDocument pdfCaseDocument =
                genericDocumentService.convertDocumentIfNotPdfAlready(consentOrder, authorisationToken, caseId);
            documents.add(pdfCaseDocument);
        }
        CaseDocument orderLetter = order.getApprovedOrder().getOrderLetter();
        if (orderLetter != null) {
            CaseDocument pdfCaseDocument =
                genericDocumentService.convertDocumentIfNotPdfAlready(orderLetter, authorisationToken, caseId);
            documents.add(pdfCaseDocument);
        }

        List<PensionTypeCollection> pensionTypeDocs = order.getApprovedOrder().getPensionDocuments();
        if (pensionTypeDocs != null && !pensionTypeDocs.isEmpty()) {
            pensionTypeDocs.forEach(pd -> {
                CaseDocument uploadedDocument = pd.getTypedCaseDocument().getPensionDocument();
                CaseDocument pdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(uploadedDocument,
                    authorisationToken, caseId);
                documents.add(pdfDocument);
            });
        }
    }


    public void addGeneratedApprovedConsentOrderDocumentsToCase(String userAuthorisation,
                                                                FinremCaseDetails finremCaseDetails) {

        String caseId = finremCaseDetails.getId().toString();
        log.info("Generating and preparing documents for latest consent order, Case ID: {}", caseId);

        StampType stampType = documentHelper.getStampType(finremCaseDetails.getData());
        CaseDocument approvedConsentOrderLetter =
            generateApprovedConsentOrderLetter(finremCaseDetails, userAuthorisation);
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        CaseDocument consentOrderAnnexStamped =
            genericDocumentService.annexStampDocument(finremCaseData.getLatestConsentOrder(),
                userAuthorisation, stampType, caseId);

        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped).build();

        List<PensionTypeCollection> pensionCollection = finremCaseData.getPensionCollection();

        if (!CollectionUtils.isEmpty(pensionCollection)) {
            log.info("Pension Documents not empty for case - "
                    + "stamping Pension Documents and adding to approvedOrder for Case ID: {}",
                caseId);
            List<PensionTypeCollection> stampedPensionDocs = stampPensionDocuments(pensionCollection,
                userAuthorisation, stampType, caseId);
            log.info("Generated StampedPensionDocs = {} for Case ID: {}", stampedPensionDocs, caseId);
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        List<ConsentOrderCollection> approvedOrders
            = Optional.ofNullable(finremCaseData.getApprovedOrderCollection()).orElse(new ArrayList<>());
        log.info("Generated ApprovedOrders = {} for Case ID {}", approvedOrders, caseId);
        ConsentOrderCollection consentOrderCollection
            = ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();
        approvedOrders.add(consentOrderCollection);
        finremCaseData.setApprovedOrderCollection(approvedOrders);

        log.info("Successfully generated documents for 'Consent Order Approved' for Case ID: {}", caseId);
    }

    public void addApprovedConsentCoverLetter(FinremCaseDetails caseDetails,
                                              List<CaseDocument> consentOrderDocumentPack,
                                              String authToken,
                                              DocumentHelper.PaperNotificationRecipient recipient) {
        final Long caseId = caseDetails.getId();
        CaseDetails bulkPrintCaseDetails = documentHelper.prepareLetterTemplateData(caseDetails, recipient);
        bulkPrintCaseDetails.getData().put(ORDER_TYPE, CONSENT);
        String approvedOrderNotificationFileName = documentConfiguration.getApprovedConsentOrderNotificationFileName();
        CaseDocument approvedCoverLetter = genericDocumentService
            .generateDocument(authToken, bulkPrintCaseDetails,
                documentConfiguration.getApprovedConsentOrderNotificationTemplate(),
                approvedOrderNotificationFileName);
        log.info("Generating approved consent order cover letter {} from {} for role {} on Case ID: {}", approvedOrderNotificationFileName,
            documentConfiguration.getApprovedConsentOrderNotificationTemplate(), recipient, caseId);
        consentOrderDocumentPack.add(approvedCoverLetter);
    }

    public boolean getApprovedOrderModifiedAfterNotApprovedOrder(ConsentOrderWrapper wrapper, String userAuthorisation) {
        CaseDocument latestRefusedConsentOrder;
        CaseDocument latestApprovedConsentOrder;
        List<ConsentOrderCollection> refusedOrders = wrapper.getConsentedNotApprovedOrders();
        List<ConsentOrderCollection> approvedOrders = wrapper.getContestedConsentedApprovedOrders();
        if (refusedOrders != null && !refusedOrders.isEmpty()) {
            latestRefusedConsentOrder = refusedOrders.get(refusedOrders.size() - 1).getApprovedOrder().getConsentOrder();
        } else {
            return approvedOrders != null && !approvedOrders.isEmpty();
        }
        if (approvedOrders != null && !approvedOrders.isEmpty()) {
            latestApprovedConsentOrder = approvedOrders.get(approvedOrders.size() - 1).getApprovedOrder().getConsentOrder();
        } else {
            return false;
        }
        return documentOrderingService.isDocumentModifiedLater(latestApprovedConsentOrder, latestRefusedConsentOrder, userAuthorisation);
    }

    public CaseDocument getPopulatedConsentCoverSheet(FinremCaseDetails caseDetails,
                                                      String authToken,
                                                      DocumentHelper.PaperNotificationRecipient recipient) {
        final Long caseId = caseDetails.getId();
        Map<String, Object> placeholdersMap = bulkPrintLetterDetailsMapper
                .getLetterDetailsAsMap(caseDetails, recipient, caseDetails.getData().getRegionWrapper().getDefaultCourtList());
        CaseDocument bulkPrintCoverSheet = genericDocumentService.generateDocumentFromPlaceholdersMap(authToken, placeholdersMap,
                documentConfiguration.getBulkPrintTemplate(), documentConfiguration.getBulkPrintFileName(),
                caseDetails.getId().toString());
        log.info("Generating consent order cover sheet {} from {} for role {} on Case ID: {}", documentConfiguration.getBulkPrintFileName(),
                documentConfiguration.getBulkPrintTemplate(), recipient, caseId);
        return bulkPrintCoverSheet;
    }
}
