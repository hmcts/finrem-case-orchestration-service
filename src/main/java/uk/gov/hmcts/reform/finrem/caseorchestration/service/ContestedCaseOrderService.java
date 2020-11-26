package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;

@Service
@RequiredArgsConstructor
public class ContestedCaseOrderService {

    private final BulkPrintService bulkPrintService;
    private final GeneralOrderService generalOrderService;
    private final FeatureToggleService featureToggleService;

    public void printAndMailGeneralOrderToParties(CaseDetails caseDetails, String authorisationToken) {
        if (featureToggleService.isContestedPrintGeneralOrderEnabled() && contestedGeneralOrderPresent(caseDetails)) {
            BulkPrintDocument generalOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData());
            if (bulkPrintService.shouldPrintForApplicant(caseDetails)) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
        }
    }

    private boolean contestedGeneralOrderPresent(CaseDetails caseDetails) {
        return !isNull(caseDetails.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
    }
}
