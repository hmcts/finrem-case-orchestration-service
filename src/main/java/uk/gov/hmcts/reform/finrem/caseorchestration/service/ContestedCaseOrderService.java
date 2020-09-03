package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;

@Service
@RequiredArgsConstructor
public class ContestedCaseOrderService {

    private final BulkPrintService bulkPrintService;
    private final GeneralOrderService generalOrderService;

    public List<String> printAndMailGeneralOrderToParties(CaseDetails caseDetails, String authorisationToken) {
        if (!contestedGeneralOrderPresent(caseDetails)) {
            return asList("No general order present in the case");
        }

        BulkPrintDocument generalOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData());
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, asList(generalOrder));
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, asList(generalOrder));

        return emptyList();
    }

    private boolean contestedGeneralOrderPresent(CaseDetails caseDetails) {
        return !isNull(caseDetails.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
    }
}
