package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremMultiLetterOrEmailAllPartiesCorresponder;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FinremContestedSendOrderCorresponder extends FinremMultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    public FinremContestedSendOrderCorresponder(NotificationService notificationService,
                                                BulkPrintService bulkPrintService, DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService, documentHelper);
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
        log.info("Sending email notification to Intervener type {} Solicitor for 'Order Approved' for case: {}",
            intervenerWrapper.getIntervenerType(), caseDetails.getId());
        notificationService.sendContestOrderApprovedEmailIntervener(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }

    @Override
    public List<CaseDocument> getCaseDocuments(FinremCaseDetails caseDetails) {
        List<OrderSentToPartiesCollection> sentToPartiesCollection = caseDetails.getData().getOrdersSentToPartiesCollection();
        List<CaseDocument> caseDocuments = new ArrayList<>();
        sentToPartiesCollection.forEach(sendOrderObj -> {
            caseDocuments.add(sendOrderObj.getValue().getCaseDocument());
        });
        return caseDocuments;
    }
}
