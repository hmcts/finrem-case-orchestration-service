package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.EmailOnlyAllLitigantsCorresponder;

@Component
@Slf4j
public class UpdateFrcEmailAllLitigantsCorresponder extends EmailOnlyAllLitigantsCorresponder {

    @Autowired
    public UpdateFrcEmailAllLitigantsCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    protected void emailApplicant(CaseDetails caseDetails) {
        log.info("Sending email notification to Applicant Solicitor for 'Update Frc information' for case: {}", caseDetails.getId());
        notificationService.sendUpdateFrcInformationEmailToAppSolicitor(caseDetails);

    }

    @Override
    protected void emailRespondent(CaseDetails caseDetails) {
        log.info("Sending email notification to Respondent Solicitor for 'Update Frc information' for case: {}", caseDetails.getId());
        notificationService.sendUpdateFrcInformationEmailToRespondentSolicitor(caseDetails);
    }

}
