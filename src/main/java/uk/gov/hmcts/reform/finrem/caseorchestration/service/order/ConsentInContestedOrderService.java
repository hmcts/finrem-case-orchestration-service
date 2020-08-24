package uk.gov.hmcts.reform.finrem.caseorchestration.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;

@Service
@RequiredArgsConstructor
public class ConsentInContestedOrderService {

    private final BulkPrintService bulkPrintService;
    private final GenericDocumentService genericDocumentService;
    private final GenerateCoverSheetService coverSheetService;
    private final DocumentHelper documentHelper;
    private final DocumentConfiguration documentConfiguration;

    public void sendConsentOrderNotApproved(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();

        List<BulkPrintDocument> orderDocuments = prepareOrderDocuments(caseData);

        printApplicantLetterPack(orderDocuments, caseDetails, authorisationToken);
        printRespondentLetterPack(orderDocuments, caseDetails, authorisationToken);
    }

    private void printRespondentLetterPack(List<BulkPrintDocument> orderDocuments, CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> respondentDocuments = new ArrayList<>();
        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
        respondentDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(respondentCoverSheet));
        respondentDocuments.addAll(orderDocuments);
        bulkPrintService.bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), respondentDocuments);
    }

    private void printApplicantLetterPack(List<BulkPrintDocument> orderDocuments, CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = new ArrayList<>();
        applicantDocuments.add(coverLetter(caseDetails, authorisationToken));
        applicantDocuments.addAll(orderDocuments);
        bulkPrintService.bulkPrintFinancialRemedyLetterPack(caseDetails.getId(), applicantDocuments);
    }

    private List<BulkPrintDocument> prepareOrderDocuments(Map<String, Object> caseData) {
        List<BulkPrintDocument> orderDocuments = new ArrayList<>();

        List<ContestedConsentOrderData> consentOrders = Optional.ofNullable(caseData.get(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION))
            .map(documentHelper::convertToContestedConsentOrderData)
            .orElse(new ArrayList<>(1));

        if (!consentOrders.isEmpty()) {
            ContestedConsentOrderData contestedConsentOrderData = consentOrders.get(consentOrders.size() - 1);
            orderDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(contestedConsentOrderData.getConsentOrder().getConsentOrder()));
        } else {
            documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, GENERAL_ORDER_LATEST_DOCUMENT).map(orderDocuments::add);
        }

        return orderDocuments;
    }

    private BulkPrintDocument coverLetter(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsWithTemplateData = documentHelper.prepareLetterToApplicantTemplateData(caseDetails);
        CaseDocument coverLetter = genericDocumentService.generateDocument(
            authorisationToken,
            caseDetailsWithTemplateData,
            documentConfiguration.getConsentOrderNotApprovedCoverLetterTemplate(),
            documentConfiguration.getConsentOrderNotApprovedCoverLetterFileName());
        return BulkPrintDocument.builder().binaryFileUrl(coverLetter.getDocumentBinaryUrl()).build();
    }
}
