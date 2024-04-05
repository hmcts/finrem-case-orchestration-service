package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
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
            .filter(i -> isEmailToIntervenerSolicitorRequired(caseDetails, i))
            .forEach(i -> emailIntervenerSolicitor(caseDetails, i));
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        boolean isConsentInContested = checkIfConsentInContestedEvent(caseDetails);
        if (caseDataService.isConsentedApplication(caseDetails)) {
            notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(caseDetails);
        } else {
            if (isConsentInContested && caseDataService.isConsentedInContestedCase(caseDetails)) {
                notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(caseDetails);
            } else {
                notificationService.sendContestedGeneralOrderEmailApplicant(caseDetails);
            }
        }
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        boolean isConsentInContested = checkIfConsentInContestedEvent(caseDetails);
        if (caseDataService.isConsentedApplication(caseDetails)) {
            notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
        } else {
            if (isConsentInContested && caseDataService.isConsentedInContestedCase(caseDetails)) {
                notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
            } else {
                notificationService.sendContestedGeneralOrderEmailRespondent(caseDetails);
            }
        }
    }

    private void emailIntervenerSolicitor(FinremCaseDetails caseDetails, IntervenerWrapper intervenerWrapper) {
        SolicitorCaseDataKeysWrapper caseDataKeysWrapper =
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper);

        boolean isConsentInContested = checkIfConsentInContestedEvent(caseDetails);

        if (isConsentInContested && caseDataService.isConsentedInContestedCase(caseDetails)) {
            notificationService.sendContestedConsentGeneralOrderEmailIntervenerSolicitor(caseDetails,
                caseDataKeysWrapper);
        } else {
            notificationService.sendContestedGeneralOrderEmailIntervener(caseDetails, caseDataKeysWrapper);
        }
    }

    private boolean checkIfConsentInContestedEvent(FinremCaseDetails caseDetails) {
        EventType eventId = caseDetails.getEventId();
        boolean isConsentInContested = caseDataService.isConsentInContestedGeneralOrderEvent(eventId);
        return isConsentInContested;
    }
}
