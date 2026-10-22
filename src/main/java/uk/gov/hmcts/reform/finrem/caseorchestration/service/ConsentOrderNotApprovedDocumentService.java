package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.VARIATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderNotApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentOrderingService documentOrderingService;
    private final CaseDataService caseDataService;
    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public List<BulkPrintDocument> prepareApplicantLetterPack(FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Generating consent order not approved documents for applicant, Case ID: {}", caseDetails.getId());

        List<BulkPrintDocument> documents = new ArrayList<>();

        documents.add(notApprovedCoverLetter(caseDetails, authorisationToken, APPLICANT));
        addEitherNotApprovedOrderOrGeneralOrderIfApplicable(caseDetails, documents, authorisationToken);

        return documents.size() == 1
            ? emptyList() // if only cover letter then print nothing
            : documents;
    }

    /**
     * Prepares the applicant's not approved documents: the cover letter followed by the not approved order
     * or the latest general order, whichever is newer. Returns an empty list if only the cover letter would be sent.
     *
     * @param caseDetails        the case details
     * @param authorisationToken the authorisation token
     * @return the applicant's documents in posting order, or an empty list if there is nothing to send
     */
    public List<CaseDocument> getApplicantLetterPackDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Preparing consent order not approved documents for applicant, Case ID: {}", caseDetails.getId());

        List<CaseDocument> documents = new ArrayList<>();

        documents.add(getNotApprovedCoverLetter(caseDetails, authorisationToken, APPLICANT));
        documents.addAll(getNotApprovedOrderOrGeneralOrder(caseDetails, authorisationToken));

        return documents.size() == 1
            ? new ArrayList<>() // if only cover letter then send nothing
            : documents;
    }

    private List<CaseDocument> getNotApprovedOrderOrGeneralOrder(FinremCaseDetails caseDetails, String authorisationToken) {
        List<CaseDocument> notApprovedOrderDocuments = notApprovedConsentOrder(caseDetails);
        Optional<CaseDocument> generalOrder = Optional.ofNullable(documentHelper.getLatestGeneralOrder(caseDetails.getData()));

        boolean useNotApprovedOrder = !notApprovedOrderDocuments.isEmpty() && (generalOrder.isEmpty()
            || documentOrderingService.isDocumentModifiedLater(notApprovedOrderDocuments.getFirst(), generalOrder.get(), authorisationToken));

        if (useNotApprovedOrder) {
            return notApprovedOrderDocuments;
        }
        return generalOrder.map(List::of).orElseGet(List::of);
    }

    private void addEitherNotApprovedOrderOrGeneralOrderIfApplicable(FinremCaseDetails finremCaseDetails, List<BulkPrintDocument> existingList,
                                                                     String authorisationToken) {
        List<CaseDocument> notApprovedOrderDocuments = notApprovedConsentOrder(finremCaseDetails);
        Optional<CaseDocument> generalOrder = Optional.ofNullable(documentHelper.getLatestGeneralOrder(finremCaseDetails.getData()));

        boolean useNotApprovedOrder = !notApprovedOrderDocuments.isEmpty() && (generalOrder.isEmpty()
            || documentOrderingService.isDocumentModifiedLater(notApprovedOrderDocuments.getFirst(), generalOrder.get(), authorisationToken));

        if (useNotApprovedOrder) {
            existingList.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(notApprovedOrderDocuments, finremCaseDetails.getCaseType(),
                authorisationToken));
        } else {
            generalOrder.ifPresent(caseDocument -> existingList.add(documentHelper.mapToBulkPrintDocument(caseDocument)));
        }
    }

    /**
     * Generates the consent order not approved cover letter for the given recipient.
     *
     * @param caseDetails        the case details used to generate the letter
     * @param authorisationToken the authorisation token
     * @param recipient          the party the cover letter is addressed to
     * @return the generated cover letter
     */
    public CaseDocument getNotApprovedCoverLetter(FinremCaseDetails caseDetails, String authorisationToken,
                                                  DocumentHelper.PaperNotificationRecipient recipient) {
        CaseDetails caseDetailsWithTemplateData = documentHelper.prepareLetterTemplateData(caseDetails, recipient);
        String notApprovedOrderNotificationFileName;
        if (consentedApplicationHelper.isVariationOrder(caseDetails.getData())) {
            notApprovedOrderNotificationFileName = documentConfiguration.getVariationOrderNotApprovedCoverLetterFileName();
            caseDetailsWithTemplateData.getData().put(ORDER_TYPE, VARIATION);
        } else {
            notApprovedOrderNotificationFileName = documentConfiguration.getConsentOrderNotApprovedCoverLetterFileName();
            caseDetailsWithTemplateData.getData().put(ORDER_TYPE, CONSENT);
        }
        return genericDocumentService.generateDocument(
            authorisationToken,
            caseDetailsWithTemplateData,
            documentConfiguration.getConsentOrderNotApprovedCoverLetterTemplate(),
            notApprovedOrderNotificationFileName);
    }

    public BulkPrintDocument notApprovedCoverLetter(FinremCaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient) {
        return documentHelper.mapToBulkPrintDocument(getNotApprovedCoverLetter(caseDetails, authorisationToken, recipient));
    }

    @SuppressWarnings("java:S3740")
    public List<CaseDocument> notApprovedConsentOrder(FinremCaseDetails caseDetails) {
        Map<String, Object> caseData = finremCaseDetailsMapper.mapToCaseDetails(caseDetails).getData();

        if (caseDataService.isContestedApplication(caseDetails)) {
            List<ContestedConsentOrderData> consentOrders = consentOrderInContestedNotApprovedList(caseData);
            if (!consentOrders.isEmpty()) {
                ContestedConsentOrderData contestedConsentOrderData = consentOrders.getLast();
                return singletonList(contestedConsentOrderData.getConsentOrder().getConsentOrder());
            }
        } else {
            log.info("Extracting 'uploadOrder' from case data for bulk print.");
            List<Map> documentList = ofNullable(caseData.get(UPLOAD_ORDER))
                .map(i -> (List<Map>) i)
                .orElse(Collections.emptyList());

            if (!documentList.isEmpty()) {
                Map<String, Object> value = ((Map) caseDataService.getLastMapValue.apply(documentList).get(VALUE));
                Optional<CaseDocument> generalOrder = documentHelper.getDocumentLinkAsCaseDocument(value, "DocumentLink");
                if (generalOrder.isPresent()) {
                    log.info("Sending general order ({}) for bulk print.", generalOrder.get().getDocumentFilename());
                    return singletonList(generalOrder.get());
                }
            }
        }

        return Collections.emptyList();
    }

    private List<ContestedConsentOrderData> consentOrderInContestedNotApprovedList(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION))
            .map(documentHelper::convertToContestedConsentOrderData)
            .orElse(new ArrayList<>(1));
    }

    public CaseDocument getLatestOrderDocument(CaseDocument refusedConsentOrder,
                                               CaseDocument latestGeneralOrder,
                                               String userAuthorisation) {
        if (refusedConsentOrder != null && latestGeneralOrder != null) {
            return documentOrderingService.isDocumentModifiedLater(latestGeneralOrder, refusedConsentOrder, userAuthorisation)
                ? latestGeneralOrder : refusedConsentOrder;
        } else if (refusedConsentOrder != null) {
            return refusedConsentOrder;
        }
        return latestGeneralOrder;
    }

    public boolean getFirstOrderModifiedAfterSecondOrder(CaseDocument firstOrder,
                                                         CaseDocument secondOrder,
                                                         String userAuthorisation) {
        if (firstOrder != null && secondOrder != null) {
            return documentOrderingService.isDocumentModifiedLater(firstOrder, secondOrder, userAuthorisation);
        } else {
            return firstOrder != null;
        }
    }

    public void addNotApprovedConsentCoverLetter(FinremCaseDetails caseDetails,
                                                 List<CaseDocument> consentOrderDocumentPack,
                                                 String authToken,
                                                 DocumentHelper.PaperNotificationRecipient recipient) {
        final Long caseId = caseDetails.getId();
        CaseDetails bulkPrintCaseDetails = documentHelper.prepareLetterTemplateData(caseDetails, recipient);
        String generalOrderNotificationFileName = documentConfiguration.getConsentOrderNotApprovedCoverLetterFileName();
        CaseDocument approvedCoverLetter = genericDocumentService
            .generateDocument(authToken, bulkPrintCaseDetails,
                documentConfiguration.getConsentOrderNotApprovedCoverLetterTemplate(),
                generalOrderNotificationFileName);
        log.info("Generating approved consent order cover letter {} from {} for role {} on Case ID: {}", generalOrderNotificationFileName,
            documentConfiguration.getConsentOrderNotApprovedCoverLetterTemplate(), recipient, caseId);
        consentOrderDocumentPack.add(approvedCoverLetter);
    }
}
