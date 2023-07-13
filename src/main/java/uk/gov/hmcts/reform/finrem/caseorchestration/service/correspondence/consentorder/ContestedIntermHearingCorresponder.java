package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsMultiLetterOrEmailAllPartiesCorresponder;

import java.util.List;

@Component
@Slf4j
public class ContestedIntermHearingCorresponder extends CaseDetailsMultiLetterOrEmailAllPartiesCorresponder {

    private final GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @Autowired
    public ContestedIntermHearingCorresponder(BulkPrintService bulkPrintService, NotificationService notificationService,
                                              FinremCaseDetailsMapper firemCaseDetailsMapper,
                                              GeneralApplicationDirectionsService generalApplicationDirectionsService) {
        super(bulkPrintService, notificationService, firemCaseDetailsMapper);
        this.generalApplicationDirectionsService = generalApplicationDirectionsService;
    }

    @Override
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Applicant Solicitor for 'interim hearing' for case: {}", caseDetails.getId());
        notificationService.sendInterimNotificationEmailToApplicantSolicitor(caseDetails);

    }

    @Override
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Respondent Solicitor for 'interim hearing' for case: {}", caseDetails.getId());
        notificationService.sendInterimNotificationEmailToRespondentSolicitor(caseDetails);
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        log.info("Sending email notification to Intervener Solicitor for 'interim hearing' for case: {}", caseDetails.getId());
        notificationService.sendInterimNotificationEmailToIntervenerSolicitor(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails, String authorisationToken) {
        return generalApplicationDirectionsService.prepareInterimHearingDocumentsToPrint(caseDetails, authorisationToken);
    }
}
