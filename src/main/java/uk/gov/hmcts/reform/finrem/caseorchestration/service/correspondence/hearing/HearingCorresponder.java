package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public abstract class HearingCorresponder extends MultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    public HearingCorresponder(BulkPrintService bulkPrintService,
                               NotificationService notificationService) {
        super(notificationService, bulkPrintService);
    }

    @Override
    public void emailApplicantSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
    }

    @Override
    public void emailRespondentSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
    }


}
