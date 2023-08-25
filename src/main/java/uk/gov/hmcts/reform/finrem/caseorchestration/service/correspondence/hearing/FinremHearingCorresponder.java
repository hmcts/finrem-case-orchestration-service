package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremMultiLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public abstract class FinremHearingCorresponder extends FinremMultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    protected FinremHearingCorresponder(BulkPrintService bulkPrintService,
                                     NotificationService notificationService) {
        super(bulkPrintService, notificationService);
    }

    @Override
    public void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
    }

    @Override
    public void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
    }

    @Override
    public void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailIntervener(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }

}
