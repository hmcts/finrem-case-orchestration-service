package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class GeneralOrderRaisedCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    private final CaseDataService caseDataService;

    @Autowired
    public GeneralOrderRaisedCorresponder(NotificationService notificationService,
                                          FinremCaseDetailsMapper firemCaseDetailsMapper,
                                          CaseDataService caseDataService) {
        super(notificationService, firemCaseDetailsMapper);
        this.caseDataService = caseDataService;
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        if (caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to applicant Solicitor for 'Consented General Order' for case id: {}", caseDetails.getId());
            notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(caseDetails);
        } else {
            if (caseDataService.isConsentedInContestedCase(caseDetails)) {
                log.info("Sending email notification to applicant Solicitor for 'Contested consent General Order' for case id: {}",
                    caseDetails.getId());
                notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(caseDetails);
            } else {
                log.info("Sending email notification to applicant solicitor for 'Contested General Order' for case id: {}", caseDetails.getId());
                notificationService.sendContestedGeneralOrderEmailApplicant(caseDetails);
            }
        }

    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        if (caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to respondent Solicitor for 'Consented General Order' for case id: {}", caseDetails.getId());
            notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
        } else {
            if (caseDataService.isConsentedInContestedCase(caseDetails)) {
                log.info("Sending email notification to respondent Solicitor for 'Contested consent General Order' for case id: {}",
                    caseDetails.getId());
                notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
            } else {
                log.info("Sending email notification to respondent solicitor for 'Contested General Order' for case id: {}", caseDetails.getId());
                notificationService.sendContestedGeneralOrderEmailRespondent(caseDetails);
            }
        }
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        if (caseDataService.isConsentedInContestedCase(caseDetails)) {
            log.info("Sending email notification to intervener Solicitor for 'Contested consent General Order' for case id: {}",
                caseDetails.getId());
            notificationService.sendContestedConsentGeneralOrderEmailIntervenerSolicitor(caseDetails,
                notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
        } else {
            log.info("Sending email notification to intervener solicitor for 'Contested General Order' for case id: {}", caseDetails.getId());
            notificationService.sendContestedGeneralOrderEmailIntervener(caseDetails,
                notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
        }

    }
}
