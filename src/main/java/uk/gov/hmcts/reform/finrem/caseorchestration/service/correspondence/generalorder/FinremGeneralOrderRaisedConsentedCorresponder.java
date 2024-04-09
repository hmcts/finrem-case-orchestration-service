package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremEmailOnlyAllSolicitorsCorresponder;

@Component
public class FinremGeneralOrderRaisedConsentedCorresponder extends FinremEmailOnlyAllSolicitorsCorresponder {

    public FinremGeneralOrderRaisedConsentedCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    public void sendCorrespondence(FinremCaseDetails caseDetails) {
        super.sendCorrespondence(caseDetails);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
    }
}
