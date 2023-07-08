package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremMultiLetterOrEmailAllPartiesCorresponder;

import java.util.List;

@Component
@Slf4j
public class FinremContestedSendOrderCorresponder extends FinremMultiLetterOrEmailAllPartiesCorresponder {

    private final GeneralOrderService generalOrderService;

    @Autowired
    public FinremContestedSendOrderCorresponder(NotificationService notificationService,
                                                BulkPrintService bulkPrintService,
                                                GeneralOrderService generalOrderService) {
        super(bulkPrintService, notificationService);
        this.generalOrderService = generalOrderService;
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isApplicantCorrespondenceEnabled()) {
            log.info("Sending email notification to Applicant Solicitor for 'Contest Order Approved' for case: {}", caseDetails.getId());
            notificationService.sendContestOrderApprovedEmailApplicant(caseDetails);
        }
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isRespondentCorrespondenceEnabled()) {
            log.info("Sending email notification to Respondent Solicitor for 'Contest Order Approved' for case: {}", caseDetails.getId());
            notificationService.sendContestOrderApprovedEmailRespondent(caseDetails);
        }
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isIntervener1CorrespondenceEnabled()) {
            log.info("Sending email notification to Intervener1 Solicitor for 'Order Approved' for case: {}", caseDetails.getId());
            IntervenerOneWrapper oneWrapper = caseDetails.getData().getIntervenerOneWrapperIfPopulated();
            notificationService.sendContestOrderApprovedEmailIntervener(caseDetails,
                notificationService.getCaseDataKeysForIntervenerSolicitor(oneWrapper));
        }
        if (caseDetails.getData().isIntervener2CorrespondenceEnabled()) {
            log.info("Sending email notification to Intervener2 Solicitor for 'Order Approved' for case: {}", caseDetails.getId());
            IntervenerTwoWrapper twoWrapper = caseDetails.getData().getIntervenerTwoWrapperIfPopulated();
            notificationService.sendContestOrderApprovedEmailIntervener(caseDetails,
                notificationService.getCaseDataKeysForIntervenerSolicitor(twoWrapper));
        }
        if (caseDetails.getData().isIntervener3CorrespondenceEnabled()) {
            log.info("Sending email notification to Intervener3 Solicitor for 'Order Approved' for case: {}", caseDetails.getId());
            IntervenerThreeWrapper threeWrapper = caseDetails.getData().getIntervenerThreeWrapperIfPopulated();
            notificationService.sendContestOrderApprovedEmailIntervener(caseDetails,
                notificationService.getCaseDataKeysForIntervenerSolicitor(threeWrapper));
        }
        if (caseDetails.getData().isIntervener4CorrespondenceEnabled()) {
            log.info("Sending email notification to Intervener4 Solicitor for 'Order Approved' for case: {}", caseDetails.getId());
            IntervenerFourWrapper fourWrapper = caseDetails.getData().getIntervenerFourWrapperIfPopulated();
            notificationService.sendContestOrderApprovedEmailIntervener(caseDetails,
                notificationService.getCaseDataKeysForIntervenerSolicitor(fourWrapper));
        }
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(FinremCaseDetails caseDetails) {
        return generalOrderService.getBulkPrintDocuments(caseDetails);
    }
}
