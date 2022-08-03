package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentOrderNotApprovedDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentOrderingService documentOrderingService;
    private final LetterDetailsMapper letterDetailsMapper;

    public List<BulkPrintDocument> prepareApplicantLetterPack(FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Generating consent order not approved documents for applicant, case ID {}", caseDetails.getId());

        List<BulkPrintDocument> documents = new ArrayList<>();

        documents.add(coverLetter(caseDetails, authorisationToken));
        addEitherNotApprovedOrderOrGeneralOrderIfApplicable(caseDetails, documents, authorisationToken);

        return documents.size() == 1
            ? emptyList()  // if only cover letter then print nothing
            : documents;
    }

    private void addEitherNotApprovedOrderOrGeneralOrderIfApplicable(FinremCaseDetails caseDetails,
                                                                     List<BulkPrintDocument> existingList,
                                                                     String authorisationToken) {
        List<Document> notApprovedOrderDocuments = notApprovedConsentOrder(caseDetails);
        Optional<Document> generalOrder = ofNullable(caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderLatestDocument());

        boolean useNotApprovedOrder = !notApprovedOrderDocuments.isEmpty() && (generalOrder.isEmpty()
            || documentOrderingService.isDocumentModifiedLater(notApprovedOrderDocuments.get(0), generalOrder.get(), authorisationToken));

        if (useNotApprovedOrder) {
            existingList.addAll(documentHelper.getDocumentsAsBulkPrintDocuments(notApprovedOrderDocuments));
            return;
        }

        generalOrder.ifPresent(document -> existingList.add(documentHelper.getDocumentAsBulkPrintDocument(document)
            .orElse(null)));
    }

    private BulkPrintDocument coverLetter(FinremCaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> placeholdersMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails, APPLICANT,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());
        Document coverLetter = genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            placeholdersMap,
            documentConfiguration.getConsentOrderNotApprovedCoverLetterTemplate(),
            documentConfiguration.getConsentOrderNotApprovedCoverLetterFileName());
        return documentHelper.getDocumentAsBulkPrintDocument(coverLetter).orElse(null);
    }

    public List<Document> notApprovedConsentOrder(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();

        if (caseData.isContestedApplication()) {
            List<ConsentOrderCollection> consentOrders = getConsentOrderInContestedNotApprovedCollection(caseData);
            if (!consentOrders.isEmpty()) {
                ConsentOrderCollection contestedConsentOrderData = Iterables.getLast(consentOrders);
                return singletonList(contestedConsentOrderData.getValue().getConsentOrder());
            }
        } else {
            log.info("Extracting 'uploadOrder' from case data for bulk print.");
            List<UploadOrderCollection> documentList = ofNullable(caseData.getUploadOrder()).orElse(emptyList());
            if (!documentList.isEmpty()) {
                UploadOrderCollection element = documentList.stream().reduce((first, second) -> second).get();
                Optional<Document> generalOrder = ofNullable(element.getValue().getDocumentLink());
                if (generalOrder.isPresent()) {
                    log.info("Sending general order ({}) for bulk print.", generalOrder.get().getFilename());
                    return singletonList(generalOrder.get());
                }
            }
        }

        return Collections.emptyList();
    }

    private List<ConsentOrderCollection> getConsentOrderInContestedNotApprovedCollection(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders())
            .orElse(new ArrayList<>());
    }
}
