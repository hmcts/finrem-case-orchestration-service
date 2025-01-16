package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.DraftOrdersNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremCorresponder;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.APPLICANT_BARRISTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.INTERVENER_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.INTERVENER_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.INTERVENER_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.INTERVENER_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy.RESPONDENT_BARRISTER;

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

    /**
     * Sends a refused order notification to the party that filed the order.
     * Notifications are sent either by email or letter.
     * If there is more than one refused order for a single party that requires a letter then these are combined into a
     * single bulk print request to avoid sending multiple letters.
     *
     * @param request the refused order correspondence request
     */
    public void sendRefusedOrder(RefusedOrderCorrespondenceRequest request) {
        sendRefusedOrderToApplicant(request);
        sendRefusedOrderToRespondent(request);
        sendRefusedOrderToApplicantBarrister(request);
        sendRefusedOrderToRespondentBarrister(request);
        sendRefusedOrderToInterveners(request);
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

    private void sendRefusedOrderToApplicantBarrister(RefusedOrderCorrespondenceRequest request) {
        if (isRefusedOrderForParty(request, APPLICANT_BARRISTER)) {
            log.info("Case {}: Sending refused order email to applicant barrister", request.getCaseId());
            sendEmail(request, APPLICANT_BARRISTER);
        }
    }

    private void sendRefusedOrderToRespondentBarrister(RefusedOrderCorrespondenceRequest request) {
        if (isRefusedOrderForParty(request, RESPONDENT_BARRISTER)) {
            log.info("Case {}: Sending refused order email to respondent barrister", request.getCaseId());
            sendEmail(request, RESPONDENT_BARRISTER);
        }
    }

    private void sendRefusedOrderToInterveners(RefusedOrderCorrespondenceRequest request) {
        FinremCaseData caseData = request.caseDetails().getData();

        sendRefusedOrderToIntervener(request, caseData.getIntervenerOne(), INTERVENER_1);
        sendRefusedOrderToIntervener(request, caseData.getIntervenerTwo(), INTERVENER_2);
        sendRefusedOrderToIntervener(request, caseData.getIntervenerThree(), INTERVENER_3);
        sendRefusedOrderToIntervener(request, caseData.getIntervenerFour(), INTERVENER_4);
    }

    private void sendRefusedOrderToIntervener(RefusedOrderCorrespondenceRequest request,
                                              IntervenerWrapper intervenerWrapper, OrderFiledBy orderFiledBy) {
        if (isRefusedOrderForParty(request, orderFiledBy)) {
            if (shouldSendIntervenerSolicitorEmail(request.caseDetails(), intervenerWrapper)) {
                log.info("Case {}: Sending refused order email to {} solicitor", request.getCaseId(),
                    intervenerWrapper.getIntervenerLabel());
                sendEmail(request, orderFiledBy);
            } else {
                log.info("Case {}: Sending refused order letter to {}", request.getCaseId(),
                    intervenerWrapper.getIntervenerLabel());
                printIntervenerLetter(request, intervenerWrapper, orderFiledBy);
            }
        }
    }

    private boolean isRefusedOrderForParty(RefusedOrderCorrespondenceRequest request, OrderFiledBy orderFiledBy) {
        return request.refusedOrders().stream()
            .anyMatch(refusedOrder -> orderFiledBy.equals(refusedOrder.getOrderFiledBy()));
    }

    private void sendEmail(RefusedOrderCorrespondenceRequest request, OrderFiledBy orderFiledBy) {
        List<RefusedOrder> refusedOrders = request.refusedOrders().stream()
            .filter(refusedOrder -> orderFiledBy.equals(refusedOrder.getOrderFiledBy()))
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

    private void printIntervenerLetter(RefusedOrderCorrespondenceRequest request, IntervenerWrapper intervenerWrapper,
                                       OrderFiledBy orderFiledBy) {
        List<BulkPrintDocument> documents = getDocumentsToPrint(request.refusedOrders(), orderFiledBy);
        bulkPrintService.printIntervenerDocuments(intervenerWrapper, request.caseDetails(),
            request.authorisationToken(), documents);
    }

    private List<BulkPrintDocument> getDocumentsToPrint(List<RefusedOrder> refusedOrders, OrderFiledBy orderFiledBy) {
        return refusedOrders.stream()
            .filter(refusedOrder -> orderFiledBy.equals(refusedOrder.getOrderFiledBy()))
            .map(RefusedOrder::getRefusalOrder)
            .map(documentHelper::getBulkPrintDocumentFromCaseDocument)
            .toList();
    }
}
