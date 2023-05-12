package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremMultiLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public abstract class FinremHearingCorresponder extends FinremMultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    public FinremHearingCorresponder(BulkPrintService bulkPrintService,
                                     NotificationService notificationService,
                                     FinremNotificationRequestMapper notificationRequestMapper) {
        super(bulkPrintService, notificationService, notificationRequestMapper);
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
    public void emailIntervenerSolicitor(FinremCaseDetails caseDetails, SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        notificationService.sendPrepareForHearingEmailIntervener(caseDetails, solicitorCaseDataKeysWrapper);
    }

}
