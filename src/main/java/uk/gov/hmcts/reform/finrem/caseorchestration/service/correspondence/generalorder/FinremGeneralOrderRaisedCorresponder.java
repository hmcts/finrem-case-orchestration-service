package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremEmailOnlyAllSolicitorsCorresponder;

@Component
public class FinremGeneralOrderRaisedCorresponder extends FinremEmailOnlyAllSolicitorsCorresponder {

    private final CaseDataService caseDataService;

    public FinremGeneralOrderRaisedCorresponder(NotificationService notificationService,
                                                CaseDataService caseDataService) {
        super(notificationService);
        this.caseDataService = caseDataService;
    }

    public void sendCorrespondence(FinremCaseDetails caseDetails) {
        super.sendCorrespondence(caseDetails);
        sendIntervenerCorrespondence(caseDetails);
    }

    private void sendIntervenerCorrespondence(FinremCaseDetails caseDetails) {
        if (!caseDataService.isContestedApplication(caseDetails)) {
            return;
        }

        caseDetails.getData().getInterveners().stream()
            .filter(i -> shouldSendIntervenerSolicitorEmail(caseDetails, i))
            .forEach(i -> emailIntervenerSolicitor(caseDetails, i));
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        if (caseDataService.isConsentedApplication(caseDetails)) {
            notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(caseDetails);
        } else {
            if (caseDataService.isConsentedInContestedCase(caseDetails)) {
                notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(caseDetails);
            } else {
                notificationService.sendContestedGeneralOrderEmailApplicant(caseDetails);
            }
        }
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        if (caseDataService.isConsentedApplication(caseDetails)) {
            notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
        } else {
            if (caseDataService.isConsentedInContestedCase(caseDetails)) {
                notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
            } else {
                notificationService.sendContestedGeneralOrderEmailRespondent(caseDetails);
            }
        }
    }

    private void emailIntervenerSolicitor(FinremCaseDetails caseDetails, IntervenerWrapper intervenerWrapper) {
        SolicitorCaseDataKeysWrapper caseDataKeysWrapper =
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper);

        if (caseDataService.isConsentedInContestedCase(caseDetails)) {
            notificationService.sendContestedConsentGeneralOrderEmailIntervenerSolicitor(caseDetails,
                caseDataKeysWrapper);
        } else {
            notificationService.sendContestedGeneralOrderEmailIntervener(caseDetails, caseDataKeysWrapper);
        }
    }
}
