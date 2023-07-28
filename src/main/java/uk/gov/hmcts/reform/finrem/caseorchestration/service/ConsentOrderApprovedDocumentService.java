package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.VARIATION;
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
    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public CaseDocument generateApprovedConsentOrderLetter(CaseDetails caseDetails, String authToken) {
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

        log.info("Generating Approved {} Order Letter {} from {} for bulk print, case: {}",
            caseData.get(ORDER_TYPE),
            fileName,
            documentConfiguration.getApprovedConsentOrderTemplate(caseDetails),
            detailsCopy.getId());

        return genericDocumentService.generateDocument(authToken,
            caseDataService.isContestedApplication(caseDetails)
                ? prepareCaseDetailsCopyForDocumentGeneratorWithContestedFields(caseDetails)
                : detailsCopy,
            documentConfiguration.getApprovedConsentOrderTemplate(caseDetails),
            fileName);
    }

    public CaseDocument generateApprovedConsentOrderCoverLetter(FinremCaseDetails caseDetails, String authToken) {
        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, APPLICANT);
        String approvedOrderNotificationFileName;
        if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(caseDetails.getData()))) {
            approvedOrderNotificationFileName = documentConfiguration.getApprovedVariationOrderNotificationFileName();
            caseDetailsForBulkPrint.getData().put(ORDER_TYPE, VARIATION);
        } else {
            approvedOrderNotificationFileName = documentConfiguration.getApprovedConsentOrderNotificationFileName();
            caseDetailsForBulkPrint.getData().put(ORDER_TYPE, CONSENT);
        }
        CaseDocument generatedApprovedConsentOrderNotificationLetter = genericDocumentService
            .generateDocument(authToken, caseDetailsForBulkPrint,
                documentConfiguration.getApprovedConsentOrderNotificationTemplate(),
                approvedOrderNotificationFileName);

        log.info("Generated Approved Consent Order cover Letter: {}", generatedApprovedConsentOrderNotificationLetter);

        return generatedApprovedConsentOrderNotificationLetter;
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

    public List<BulkPrintDocument> prepareApplicantLetterPack(FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Sending Approved Consent Order to applicant / solicitor for Bulk Print, case {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        if (caseDataService.isPaperApplication(caseData)) {
            CaseDocument coverLetter = generateApprovedConsentOrderCoverLetter(caseDetails, authorisationToken);
            bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(coverLetter));
        }

        bulkPrintDocuments.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(
            approvedOrderDocuments(caseDetails, authorisationToken)));

        return bulkPrintDocuments;
    }

    public void stampAndPopulateContestedConsentApprovedOrderCollection(Map<String, Object> caseData, String authToken, String caseId) {
        CaseDocument stampedAndAnnexedDoc = stampAndAnnexContestedConsentOrder(caseData, authToken, caseId);
        List<PensionTypeCollection> pensionDocs = consentInContestedStampPensionDocuments(caseData, authToken, caseId);
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

    private CaseDocument stampAndAnnexContestedConsentOrder(Map<String, Object> caseData,
                                                            String authToken,
                                                            String caseId) {
        CaseDocument latestConsentOrder = getLatestConsentInContestedConsentOrder(caseData);
        CaseDocument pdfDocument =
            genericDocumentService.convertDocumentIfNotPdfAlready(latestConsentOrder, authToken, caseId);
        caseData.put(CONSENT_ORDER, pdfDocument);
        StampType stampType = documentHelper.getStampType(caseData);
        CaseDocument stampedDoc = genericDocumentService.stampDocument(pdfDocument, authToken, stampType, caseId);
        CaseDocument stampedAndAnnexedDoc =
            genericDocumentService.annexStampDocument(stampedDoc, authToken, stampType, caseId);
        log.info("Stamped Document and Annex doc = {}", stampedAndAnnexedDoc);
        return stampedAndAnnexedDoc;
    }

    private void populateContestedConsentOrderCaseDetails(Map<String, Object> caseData, CaseDocument stampedDoc,
                                                          List<PensionTypeCollection> pensionDocs) {
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
        return mapper.convertValue(caseData.get(CONSENT_ORDER), new TypeReference<>() {
        });
    }

    private List<PensionTypeCollection> consentInContestedStampPensionDocuments(Map<String, Object> caseData,
                                                                                String authToken,
                                                                                String caseId) {
        List<PensionTypeCollection> pensionDocs = getContestedConsentPensionDocuments(caseData);
        StampType stampType = documentHelper.getStampType(caseData);
        return stampPensionDocuments(pensionDocs, authToken, stampType, caseId);
    }

    private List<PensionTypeCollection> getContestedConsentPensionDocuments(Map<String, Object> caseData) {
        if (ObjectUtils.isEmpty(caseData.get(CONTESTED_CONSENT_PENSION_COLLECTION))) {
            return new ArrayList<>();
        }
        return mapper.convertValue(caseData.get(CONTESTED_CONSENT_PENSION_COLLECTION), new TypeReference<>() {
        });
    }

    List<CollectionElement<ApprovedOrder>> getConsentInContestedApprovedOrderCollection(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(CONTESTED_CONSENT_ORDER_COLLECTION), new TypeReference<>() {
        });
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

    public List<CaseDocument> approvedOrderDocuments(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        Map<String, Object> caseData = caseDetails.getData();
        List<CaseDocument> documents = new ArrayList<>();
        String approvedOrderCollectionFieldName = caseDataService.isConsentedInContestedCase(caseDetails)
            ? CONTESTED_CONSENT_ORDER_COLLECTION : APPROVED_ORDER_COLLECTION;

        List<ConsentOrderCollection> convertedData = new ArrayList<>();
        List<ConsentOrderCollection> approvedOrderList = covert(caseData.get(approvedOrderCollectionFieldName));
        if (!approvedOrderList.isEmpty()) {
            approvedOrderList.forEach(order -> bulkPrintDocuments(order,
                caseData, documents, convertedData, approvedOrderCollectionFieldName,
                authorisationToken, caseDetails.getId().toString()));
        }

        return documents;
    }

    private void bulkPrintDocuments(ConsentOrderCollection order, Map<String, Object> caseData,
                                    List<CaseDocument> documents,
                                    List<ConsentOrderCollection> convertedData,
                                    String approvedOrderCollectionFieldName,
                                    String authorisationToken, String caseId) {

        ApprovedOrder.ApprovedOrderBuilder consentOrderHolder = ApprovedOrder.builder();

        CaseDocument consentOrder = order.getApprovedOrder().getConsentOrder();
        if (consentOrder != null) {
            CaseDocument pdfCaseDocument =
                genericDocumentService.convertDocumentIfNotPdfAlready(consentOrder, authorisationToken, caseId);
            documents.add(pdfCaseDocument);
            consentOrderHolder.consentOrder(pdfCaseDocument);
        }
        CaseDocument orderLetter = order.getApprovedOrder().getOrderLetter();
        if (orderLetter != null) {
            CaseDocument pdfCaseDocument =
                genericDocumentService.convertDocumentIfNotPdfAlready(orderLetter, authorisationToken, caseId);
            documents.add(pdfCaseDocument);
            consentOrderHolder.orderLetter(pdfCaseDocument);
        }

        List<PensionTypeCollection> pensionTypeDocuments = new ArrayList<>();
        List<PensionTypeCollection> pensionTypeDocs = covertPensionType(order.getApprovedOrder().getPensionDocuments());
        if (!pensionTypeDocs.isEmpty()) {
            pensionTypeDocs.forEach(pd -> {
                PensionDocumentType typeOfDocument = pd.getTypedCaseDocument().getTypeOfDocument();
                CaseDocument uploadedDocument = pd.getTypedCaseDocument().getPensionDocument();
                CaseDocument pdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(uploadedDocument, authorisationToken, caseId);
                documents.add(pdfDocument);
                PensionTypeCollection ptc = PensionTypeCollection
                    .builder()
                    .typedCaseDocument(PensionType
                        .builder()
                        .typeOfDocument(typeOfDocument)
                        .pensionDocument(pdfDocument)
                        .build())
                    .build();
                pensionTypeDocuments.add(ptc);
            });
            consentOrderHolder.pensionDocuments(pensionTypeDocuments);
        }
        ConsentOrderCollection consentOrderCollection = ConsentOrderCollection
            .builder()
            .approvedOrder(consentOrderHolder.build())
            .build();
        convertedData.add(consentOrderCollection);
        caseData.put(approvedOrderCollectionFieldName, convertedData);
    }

    public void generateConsentInContestedBulkPrintDocuments(ConsentOrderCollection order, FinremCaseData caseData,
                                                             List<CaseDocument> documents,
                                                             List<ConsentOrderCollection> convertedData,
                                                             String authorisationToken, String caseId) {

        //ApprovedOrder.ApprovedOrderBuilder consentOrderHolder = ApprovedOrder.builder();

        CaseDocument consentOrder = order.getApprovedOrder().getConsentOrder();
        if (consentOrder != null) {
            CaseDocument pdfCaseDocument =
                genericDocumentService.convertDocumentIfNotPdfAlready(consentOrder, authorisationToken, caseId);
            documents.add(pdfCaseDocument);
            //consentOrderHolder.consentOrder(pdfCaseDocument);
            order.getApprovedOrder().setConsentOrder(pdfCaseDocument);
        }
        CaseDocument orderLetter = order.getApprovedOrder().getOrderLetter();
        if (orderLetter != null) {
            CaseDocument pdfCaseDocument =
                genericDocumentService.convertDocumentIfNotPdfAlready(orderLetter, authorisationToken, caseId);
            documents.add(pdfCaseDocument);
            //consentOrderHolder.orderLetter(pdfCaseDocument);
            order.getApprovedOrder().setOrderLetter(pdfCaseDocument);
        }
        CaseDocument additionalDocument = caseData.getAdditionalConsentInContestedDocument();
        if (additionalDocument != null) {
            CaseDocument pdfCaseDocument =
                genericDocumentService.convertDocumentIfNotPdfAlready(additionalDocument, authorisationToken, caseId);
            documents.add(pdfCaseDocument);
            //consentOrderHolder.orderLetter(pdfCaseDocument);
            caseData.setAdditionalConsentInContestedDocument(pdfCaseDocument);
        }

        List<PensionTypeCollection> pensionTypeDocuments = new ArrayList<>();
        List<PensionTypeCollection> pensionTypeDocs = covertPensionType(order.getApprovedOrder().getPensionDocuments());
        if (!pensionTypeDocs.isEmpty()) {
            pensionTypeDocs.forEach(pd -> {
                PensionDocumentType typeOfDocument = pd.getTypedCaseDocument().getTypeOfDocument();
                CaseDocument uploadedDocument = pd.getTypedCaseDocument().getPensionDocument();
                CaseDocument pdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(uploadedDocument, authorisationToken, caseId);
                documents.add(pdfDocument);
                PensionTypeCollection ptc = PensionTypeCollection
                    .builder()
                    .typedCaseDocument(PensionType
                        .builder()
                        .typeOfDocument(typeOfDocument)
                        .pensionDocument(pdfDocument)
                        .build())
                    .build();
                pensionTypeDocuments.add(ptc);
            });
            //consentOrderHolder.pensionDocuments(pensionTypeDocuments);
            order.getApprovedOrder().setPensionDocuments(pensionTypeDocs);
        }
//        ConsentOrderCollection consentOrderCollection = ConsentOrderCollection
//            .builder()
//            .approvedOrder(consentOrderHolder.build())
//            .build();
//        convertedData.add(consentOrderCollection);
//        caseData.getConsentOrderWrapper().setContestedConsentedApprovedOrders(convertedData);
    }


    public List<ConsentOrderCollection> covert(Object object) {
        if (object == null) {
            return Collections.emptyList();
        }
        return mapper.registerModule(new JavaTimeModule()).convertValue(object, new TypeReference<>() {
        });
    }

    private List<PensionTypeCollection> covertPensionType(Object object) {
        if (object == null) {
            return Collections.emptyList();
        }
        return mapper.registerModule(new JavaTimeModule()).convertValue(object, new TypeReference<>() {
        });
    }

    public List<CaseDocument> approvedOrderCollection(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> data = caseDetails.getData();
        List<CaseDocument> documents = new ArrayList<>();
        String approvedOrderCollectionFieldName = caseDataService.isConsentedInContestedCase(caseDetails)
            ? CONTESTED_CONSENT_ORDER_COLLECTION : APPROVED_ORDER_COLLECTION;

        List<Map> approvedOrderCollectionData = ofNullable(data.get(approvedOrderCollectionFieldName))
            .map(List.class::cast)
            .orElse(new ArrayList<>());

        if (!approvedOrderCollectionData.isEmpty()) {
            log.info("Extracting '{}' from case data for bulk print, case {}", approvedOrderCollectionFieldName, caseDetails.getId());
            Map<String, Object> lastApprovedOrder = (Map<String, Object>) (approvedOrderCollectionData.get(approvedOrderCollectionData.size() - 1)
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

    public void addGeneratedApprovedConsentOrderDocumentsToCase(String userAuthorisation,
                                                                FinremCaseDetails finremCaseDetails) {

        String caseId = finremCaseDetails.getId().toString();
        log.info("Generating and preparing documents for latest consent order, case {}", caseId);
        CaseDetails generateDocumentPayload = null;
        try {
            generateDocumentPayload = mapper.readValue(mapper.writeValueAsString(finremCaseDetails), CaseDetails.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        StampType stampType = documentHelper.getStampType(finremCaseDetails.getData());
        CaseDocument approvedConsentOrderLetter =
            generateApprovedConsentOrderLetter(generateDocumentPayload, userAuthorisation);
        CaseDocument consentOrderAnnexStamped =
            genericDocumentService.annexStampDocument(finremCaseDetails.getData().getLatestConsentOrder(),
                userAuthorisation, stampType, caseId);

        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped).build();

        List<PensionTypeCollection> consentPensionCollection =
            finremCaseDetails.getData().getConsentPensionCollection();

        if (!CollectionUtils.isEmpty(consentPensionCollection)) {
            log.info("Pension Documents not empty for case - "
                    + "stamping Pension Documents and adding to approvedOrder for case {}",
                caseId);
            List<PensionTypeCollection> stampedPensionDocs = stampPensionDocuments(consentPensionCollection,
                userAuthorisation, stampType, caseId);
            log.info("Generated StampedPensionDocs = {} for case {}", stampedPensionDocs, caseId);
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        List<ConsentOrderCollection> approvedOrders = singletonList(ConsentOrderCollection.<ApprovedOrder>builder()
            .approvedOrder(approvedOrder).build());
        log.info("Generated ApprovedOrders = {} for case {}", approvedOrders, caseId);

        finremCaseDetails.getData().setApprovedOrderCollection(approvedOrders);

        log.info("Successfully generated documents for 'Consent Order Approved' for case {}", caseId);
    }
}
