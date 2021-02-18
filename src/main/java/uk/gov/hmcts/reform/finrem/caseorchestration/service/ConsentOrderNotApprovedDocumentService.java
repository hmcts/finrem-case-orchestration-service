package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
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

    public List<BulkPrintDocument> prepareApplicantLetterPack(CaseDetails caseDetails, String authorisationToken) {
        log.info("Generating consent order not approved documents for applicant, case ID {}", caseDetails.getId());

        List<BulkPrintDocument> documents = new ArrayList<>();

        documents.add(coverLetter(caseDetails, authorisationToken));
        addEitherNotApprovedOrderOrGeneralOrderIfApplicable(caseDetails, documents, authorisationToken);

        return documents.size() == 1
            ? emptyList()  // if only cover letter then print nothing
            : documents;
    }

    private void addEitherNotApprovedOrderOrGeneralOrderIfApplicable(CaseDetails caseDetails, List<BulkPrintDocument> existingList,
                                                                     String authorisationToken) {
        List<CaseDocument> notApprovedOrderDocuments = notApprovedConsentOrder(caseDetails);
        Optional<CaseDocument> generalOrder = Optional.ofNullable(documentHelper.getLatestGeneralOrder(caseDetails.getData()));

        boolean useNotApprovedOrder = !notApprovedOrderDocuments.isEmpty() && (generalOrder.isEmpty()
            || documentOrderingService.isDocumentModifiedLater(notApprovedOrderDocuments.get(0), generalOrder.get(), authorisationToken));

        if (useNotApprovedOrder) {
            existingList.addAll(documentHelper.getCaseDocumentsAsBulkPrintDocuments(notApprovedOrderDocuments));
        } else if (generalOrder.isPresent()) {
            existingList.add(documentHelper.getCaseDocumentAsBulkPrintDocument(generalOrder.get()));
        }
    }

    private BulkPrintDocument coverLetter(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsWithTemplateData = documentHelper.prepareLetterTemplateData(caseDetails, APPLICANT);
        CaseDocument coverLetter = genericDocumentService.generateDocument(
            authorisationToken,
            caseDetailsWithTemplateData,
            documentConfiguration.getConsentOrderNotApprovedCoverLetterTemplate(),
            documentConfiguration.getConsentOrderNotApprovedCoverLetterFileName());
        return documentHelper.getCaseDocumentAsBulkPrintDocument(coverLetter);
    }

    public List<CaseDocument> notApprovedConsentOrder(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isContestedApplication(caseDetails)) {
            List<ContestedConsentOrderData> consentOrders = consentOrderInContestedNotApprovedList(caseData);
            if (!consentOrders.isEmpty()) {
                ContestedConsentOrderData contestedConsentOrderData = consentOrders.get(consentOrders.size() - 1);
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
}
