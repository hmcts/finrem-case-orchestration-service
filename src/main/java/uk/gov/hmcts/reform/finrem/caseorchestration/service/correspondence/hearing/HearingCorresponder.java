package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsMultiLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public abstract class HearingCorresponder extends CaseDetailsMultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    public HearingCorresponder(BulkPrintService bulkPrintService,
                               NotificationService notificationService,
                               FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(bulkPrintService, notificationService, finremCaseDetailsMapper);
    }

    @Override
    public void emailApplicantSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
    }

    @Override
    public void emailRespondentSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
    }

    @Override
    public void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailIntervener(intervenerWrapper, caseDetails);
    }


}
