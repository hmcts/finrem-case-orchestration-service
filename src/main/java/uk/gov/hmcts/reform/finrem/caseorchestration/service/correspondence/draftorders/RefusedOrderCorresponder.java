package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.DraftOrdersNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremCorresponder;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderParty.RESPONDENT;

@Component
@Slf4j
public class RefusedOrderCorresponder extends FinremCorresponder {
    private final DraftOrdersNotificationRequestMapper notificationRequestMapper;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;

    public RefusedOrderCorresponder(NotificationService notificationService,
                                    DraftOrdersNotificationRequestMapper notificationRequestMapper,
                                    DocumentHelper documentHelper, BulkPrintService bulkPrintService) {
        super(notificationService);
        this.notificationRequestMapper = notificationRequestMapper;
        this.documentHelper = documentHelper;
        this.bulkPrintService = bulkPrintService;
    }

    public void sendRefusedOrder(RefusedOrderCorrespondenceRequest request) {
        if (isApplicantCorrespondenceEnabled(request.caseDetails())) {
            sendRefusedOrderToApplicant(request);
        }
        if (isRespondentCorrespondenceEnabled(request.caseDetails())) {
            sendRefusedOrderToRespondent(request);
        }
    }

    private void sendRefusedOrderToApplicant(RefusedOrderCorrespondenceRequest request) {
        if (isRefusedOrderForParty(request, APPLICANT)) {
            if (shouldSendApplicantSolicitorEmail(request.caseDetails())) {
                log.info("Case {}: Sending refused order email to applicant solicitor", request.getCaseId());
                sendEmail(request, APPLICANT);
            } else {
                log.info("Case {}: Sending refused order letter to applicant", request.getCaseId());
                printApplicantLetter(request);
            }
        }
    }

    private void sendRefusedOrderToRespondent(RefusedOrderCorrespondenceRequest request) {
        if (isRefusedOrderForParty(request, RESPONDENT)) {
            if (shouldSendRespondentSolicitorEmail(request.caseDetails())) {
                log.info("Case {}: Sending refused order email to respondent solicitor", request.getCaseId());
                sendEmail(request, RESPONDENT);
            } else {
                log.info("Case {}: Sending refused order letter to respondent", request.getCaseId());
                printRespondentLetter(request);
            }
        }
    }

    private boolean isRefusedOrderForParty(RefusedOrderCorrespondenceRequest request, OrderParty orderParty) {
        return request.refusedOrders().stream()
            .anyMatch(refusedOrder -> orderParty.equals(refusedOrder.getOrderParty()));
    }

    private void sendEmail(RefusedOrderCorrespondenceRequest request, OrderParty orderParty) {
        List<RefusedOrder> refusedOrders = request.refusedOrders().stream()
            .filter(refusedOrder -> orderParty.equals(refusedOrder.getOrderParty()))
            .toList();

        refusedOrders.forEach(refusedOrder -> {
            NotificationRequest notificationRequest = notificationRequestMapper
                .buildRefusedDraftOrderOrPsaNotificationRequest(request.caseDetails(), refusedOrder);

            notificationService.sendRefusedDraftOrderOrPsa(notificationRequest);
        });
    }

    private void printApplicantLetter(RefusedOrderCorrespondenceRequest request) {
        List<BulkPrintDocument> documents = getDocumentsToPrint(request.refusedOrders(), APPLICANT);
        bulkPrintService.printApplicantDocuments(request.caseDetails(), request.authorisationToken(), documents);
    }

    private void printRespondentLetter(RefusedOrderCorrespondenceRequest request) {
        List<BulkPrintDocument> documents = getDocumentsToPrint(request.refusedOrders(), RESPONDENT);
        bulkPrintService.printRespondentDocuments(request.caseDetails(), request.authorisationToken(), documents);
    }

    private List<BulkPrintDocument> getDocumentsToPrint(List<RefusedOrder> refusedOrders, OrderParty orderParty) {
        return refusedOrders.stream()
            .filter(refusedOrder -> orderParty.equals(refusedOrder.getOrderParty()))
            .map(RefusedOrder::getRefusalOrder)
            .map(documentHelper::getBulkPrintDocumentFromCaseDocument)
            .toList();
    }
}
