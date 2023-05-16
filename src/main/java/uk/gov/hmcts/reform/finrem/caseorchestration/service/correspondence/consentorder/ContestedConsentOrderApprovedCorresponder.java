package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class ContestedConsentOrderApprovedCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public ContestedConsentOrderApprovedCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Applicant Solicitor for 'Contested Consent Order Approved' for case: {}", caseDetails.getId());
        notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Respondent Solicitor for 'Contested Consent Order Approved' for case: {}", caseDetails.getId());
        notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(caseDetails);
    }

    @Override
    protected void emailIntervenerSolicitor(CaseDetails caseDetails, SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        log.info("Sending email notification to Intervener Solicitor for 'Contested Consent Order Approved' for case: {}", caseDetails.getId());
        notificationService.sendContestedConsentOrderApprovedEmailToIntervenerSolicitor(caseDetails, solicitorCaseDataKeysWrapper);
    }
}
