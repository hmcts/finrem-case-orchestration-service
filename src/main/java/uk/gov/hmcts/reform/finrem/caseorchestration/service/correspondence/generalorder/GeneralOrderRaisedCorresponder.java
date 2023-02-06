package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class GeneralOrderRaisedCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    private final CaseDataService caseDataService;

    @Autowired
    public GeneralOrderRaisedCorresponder(NotificationService notificationService,
                                          CaseDataService caseDataService) {
        super(notificationService);
        this.caseDataService = caseDataService;
    }

    @Override
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
}
