package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedPaperApplication;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestedCaseOrderService {

    private final BulkPrintService bulkPrintService;
    private final GeneralOrderService generalOrderService;
    private final FeatureToggleService featureToggleService;
    private final DocumentHelper documentHelper;

    public void printAndMailGeneralOrderToParties(CaseDetails caseDetails, String authorisationToken) {
        if (featureToggleService.isContestedPrintGeneralOrderEnabled() && contestedGeneralOrderPresent(caseDetails)) {
            BulkPrintDocument generalOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData());
            if (bulkPrintService.shouldPrintForApplicant(caseDetails)) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
        }
    }

    public void printAndMailHearingDocuments(CaseDetails caseDetails, String authorisationToken) {
        if (isContestedPaperApplication(caseDetails)) {
            Map<String, Object> caseData = caseDetails.getData();

            if (bulkPrintService.shouldPrintForApplicant(caseDetails)) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, createHearingDocumentPack(caseData));
                log.info("Received request to send hearing pack for applicant for case{}:", caseDetails.getId());
            }

            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, createHearingDocumentPack(caseData));
            log.info("Received request to send hearing pack for respondent for case{}:", caseDetails.getId());
        }
    }

    private List<BulkPrintDocument> createHearingDocumentPack(Map<String, Object> caseData) {
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, LATEST_DRAFT_HEARING_ORDER).ifPresent(hearingDocumentPack::add);

        Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
        latestAdditionalHearingDocument.ifPresent(
            caseDocument -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument)));

        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getCollectionOfDocumentLinksAsBulkPrintDocuments(
            caseData, HEARING_ORDER_OTHER_COLLECTION);

        if (otherHearingDocuments != null) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }

        return hearingDocumentPack;
    }

    private boolean contestedGeneralOrderPresent(CaseDetails caseDetails) {
        return !isNull(caseDetails.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
    }

}
