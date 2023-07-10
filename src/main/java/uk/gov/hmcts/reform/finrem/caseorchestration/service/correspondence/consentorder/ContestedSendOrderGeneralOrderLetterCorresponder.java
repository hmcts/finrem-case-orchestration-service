package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsMultiLetterOnlyAllPartiesCorresponder;

import java.util.List;

@Component
@Slf4j
public class ContestedSendOrderGeneralOrderLetterCorresponder extends CaseDetailsMultiLetterOnlyAllPartiesCorresponder {

    private final GeneralOrderService generalOrderService;

    @Autowired
    public ContestedSendOrderGeneralOrderLetterCorresponder(BulkPrintService bulkPrintService,
                                                            NotificationService notificationService,
                                                            FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                            GeneralOrderService generalOrderService) {
        super(bulkPrintService, notificationService, finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails, String authorisationToken) {
        BulkPrintDocument generalOrder =
            generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData(), authorisationToken, caseDetails.getId().toString());
        return List.of(generalOrder);
    }



}
