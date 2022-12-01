package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailAllLitigantsCorresponder;

@Component
@Slf4j
public abstract class HearingCorresponder extends MultiLetterOrEmailAllLitigantsCorresponder {

    @Autowired
    public HearingCorresponder(BulkPrintService bulkPrintService,
                               NotificationService notificationService) {
        super(notificationService, bulkPrintService);
    }

    @Override
    protected void emailApplicant(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
    }

    @Override
    protected void emailRespondent(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
    }


}
