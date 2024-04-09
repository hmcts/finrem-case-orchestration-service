package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremEmailOnlyAllSolicitorsCorresponder;

@Component
public class FinremGeneralOrderRaisedContestedCorresponder extends FinremEmailOnlyAllSolicitorsCorresponder {

    public FinremGeneralOrderRaisedContestedCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    public void sendCorrespondence(FinremCaseDetails caseDetails) {
        super.sendCorrespondence(caseDetails);
        sendIntervenerCorrespondence(caseDetails);
    }

    private void sendIntervenerCorrespondence(FinremCaseDetails caseDetails) {
        caseDetails.getData().getInterveners().stream()
            .filter(i -> isEmailToIntervenerSolicitorRequired(caseDetails, i))
            .forEach(i -> emailIntervenerSolicitor(caseDetails, i));
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendContestedGeneralOrderEmailApplicant(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendContestedGeneralOrderEmailRespondent(caseDetails);
    }

    private void emailIntervenerSolicitor(FinremCaseDetails caseDetails, IntervenerWrapper intervenerWrapper) {
        SolicitorCaseDataKeysWrapper caseDataKeysWrapper =
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper);
        notificationService.sendContestedGeneralOrderEmailIntervener(caseDetails, caseDataKeysWrapper);
    }
}
