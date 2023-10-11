package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsMultiLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public abstract class HearingCorresponder extends CaseDetailsMultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    protected HearingCorresponder(BulkPrintService bulkPrintService,
                                  NotificationService notificationService,
                                  FinremCaseDetailsMapper finremCaseDetailsMapper,
                                  DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService, finremCaseDetailsMapper, documentHelper);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void emailApplicantSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void emailRespondentSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailIntervener(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }


}
