package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class ContestedSendOrderCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public ContestedSendOrderCorresponder(NotificationService notificationService,
                                          FinremCaseDetailsMapper firemCaseDetailsMapper) {
        super(notificationService, firemCaseDetailsMapper);
    }

    @Override
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        if (notificationService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Contest Order Approved' for case: {}", caseDetails.getId());
            notificationService.sendContestOrderApprovedEmailApplicant(caseDetails);
        }
    }

    @Override
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseDetails.getData())) {
            log.info("Sending email notification to Respondent Solicitor for 'Contest Order Approved' for case: {}", caseDetails.getId());
            notificationService.sendContestOrderApprovedEmailRespondent(caseDetails);
        }
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        log.info("Sending email notification to Intervener Solicitor for 'Contest Order Approved' for case: {}", caseDetails.getId());
        notificationService.sendContestOrderApprovedEmailIntervener(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }
}
