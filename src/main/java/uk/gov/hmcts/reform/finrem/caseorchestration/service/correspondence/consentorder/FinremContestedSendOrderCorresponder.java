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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui.CitizenPinInPostPersistenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremMultiLetterOrEmailAllPartiesCorresponder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@Component
@Slf4j
public class FinremContestedSendOrderCorresponder extends FinremMultiLetterOrEmailAllPartiesCorresponder {

    private final CitizenPinInPostPersistenceService citizenPinInPostPersistenceService;

    @Autowired
    public FinremContestedSendOrderCorresponder(NotificationService notificationService,
                                                BulkPrintService bulkPrintService, DocumentHelper documentHelper,
                                                CitizenPinInPostPersistenceService citizenPinInPostPersistenceService) {

        super(bulkPrintService, notificationService, documentHelper);
        this.citizenPinInPostPersistenceService = citizenPinInPostPersistenceService;
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isApplicantCorrespondenceEnabled()) {
            log.info("Sending email notification to Applicant Solicitor for 'Contest Order Approved' for Case ID: {}", caseDetails.getId());
            notificationService.sendContestOrderApprovedEmailApplicant(caseDetails);
        }
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        if (caseDetails.getData().isRespondentCorrespondenceEnabled()) {
            log.info("Sending email notification to Respondent Solicitor for 'Contest Order Approved' for Case ID: {}", caseDetails.getId());
            notificationService.sendContestOrderApprovedEmailRespondent(caseDetails);
        }
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        log.info("Sending email notification to Intervener type {} Solicitor for 'Order Approved' for Case ID: {}",
            intervenerWrapper.getIntervenerType(), caseDetails.getId());
        notificationService.sendContestOrderApprovedEmailIntervener(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper),
            intervenerWrapper.getIntervenerType());
    }

    @Override
    public List<CaseDocument> getCaseDocuments(FinremCaseDetails caseDetails) {
        List<OrderSentToPartiesCollection> sentToPartiesCollection = caseDetails.getData().getOrdersSentToPartiesCollection();
        List<CaseDocument> caseDocuments = new ArrayList<>();
        sentToPartiesCollection.forEach(sendOrderObj -> caseDocuments.add(sendOrderObj.getValue().getCaseDocument()));
        return caseDocuments;
    }

    @Override
    protected void printApplicantDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        citizenPinInPostPersistenceService.ensurePinPersisted(caseDetails, APPLICANT);
        super.printApplicantDocuments(authorisationToken, caseDetails);
    }

    @Override
    protected void printRespondentDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        citizenPinInPostPersistenceService.ensurePinPersisted(caseDetails, RESPONDENT);
        super.printRespondentDocuments(authorisationToken, caseDetails);
    }
}
