package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedConsentOrderApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedConsentOrderNotApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentInContestedSendOrderCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder.GeneralOrderRaisedCorresponder;

import java.util.List;

@Slf4j
@Service
public class SendConsentOrderInContestedSubmittedHandler extends FinremCallbackHandler {
    private final GeneralOrderService generalOrderService;
    private final GeneralOrderRaisedCorresponder generalOrderRaisedCorresponder;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final FinremConsentInContestedSendOrderCorresponder contestedSendOrderCorresponder;
    private final ContestedConsentOrderApprovedCorresponder contestedConsentOrderApprovedCorresponder;
    private final ContestedConsentOrderNotApprovedCorresponder contestedConsentOrderNotApprovedCorresponder;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    public SendConsentOrderInContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       GeneralOrderService generalOrderService,
                                                       FinremConsentInContestedSendOrderCorresponder contestedSendOrderCorresponder,
                                                       ContestedConsentOrderApprovedCorresponder contestedConsentOrderApprovedCorresponder,
                                                       GeneralOrderRaisedCorresponder generalOrderRaisedCorresponder,
                                                       ContestedConsentOrderNotApprovedCorresponder contestedConsentOrderNotApprovedCorresponder,
                                                       ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                       ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService
                                                       ) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.finremCaseDetailsMapper = finremCaseDetailsMapper;
        this.contestedSendOrderCorresponder = contestedSendOrderCorresponder;
        this.contestedConsentOrderApprovedCorresponder = contestedConsentOrderApprovedCorresponder;
        this.generalOrderRaisedCorresponder = generalOrderRaisedCorresponder;
        this.contestedConsentOrderNotApprovedCorresponder = contestedConsentOrderNotApprovedCorresponder;
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.consentOrderNotApprovedDocumentService = consentOrderNotApprovedDocumentService;
    }


    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_CONSENT_IN_CONTESTED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        log.info("Invoking contested {} submitted callback for case id: {}", callbackRequest.getEventType(), finremCaseDetails.getId());

        List<String> parties = generalOrderService.getParties(finremCaseDetails);
        log.info("Selected parties {} on case {}", parties, finremCaseDetails.getId());

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        ConsentOrderWrapper wrapper = finremCaseDetails.getData().getConsentOrderWrapper();
        CaseDocument latestGeneralOrder = finremCaseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();
        if (getApprovedOrderModifiedLatest(wrapper, latestGeneralOrder, userAuthorisation)) {
            log.info("Approved consent order modified after general and refused orders for case {}", finremCaseDetails.getId());
            contestedConsentOrderApprovedCorresponder.sendCorrespondence(caseDetails);
        } else if (getRefusedOrderModifiedLatest(wrapper, latestGeneralOrder, userAuthorisation)) {
            log.info("Refused consent order modified after general and approved orders for case {}", finremCaseDetails.getId());
            contestedConsentOrderNotApprovedCorresponder.sendCorrespondence(caseDetails);
        } else {
            log.info("General order modified after approved and refused consent orders for case {}", finremCaseDetails.getId());
            generalOrderRaisedCorresponder.sendCorrespondence(caseDetails);
        }

        sendNotifications(finremCaseDetails, parties, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseDetails.getData()).build();
    }

    private void sendNotifications(FinremCaseDetails caseDetails, List<String> parties, String userAuthorisation) {
        generalOrderService.setPartiesToReceiveCommunication(caseDetails, parties);
        log.info("About to start send order correspondence for case {}", caseDetails.getId());
        contestedSendOrderCorresponder.sendCorrespondence(caseDetails, userAuthorisation);
        log.info("Finish sending order correspondence for case {}", caseDetails.getId());
    }

    private boolean getApprovedOrderModifiedLatest(ConsentOrderWrapper wrapper, CaseDocument latestGeneralOrder, String userAuth) {
        List<ConsentOrderCollection> approvedOrders = wrapper.getContestedConsentedApprovedOrders();
        CaseDocument latestApprovedConsentOrder = null;
        if (approvedOrders != null && !approvedOrders.isEmpty()) {
            latestApprovedConsentOrder = approvedOrders.get(approvedOrders.size() - 1).getApprovedOrder().getConsentOrder();
        }
        return consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, userAuth)
            && consentOrderNotApprovedDocumentService.getFirstOrderModifiedAfterSecondOrder(
                    latestApprovedConsentOrder, latestGeneralOrder, userAuth);
    }

    private boolean getRefusedOrderModifiedLatest(ConsentOrderWrapper wrapper, CaseDocument latestGeneralOrder, String userAuth) {
        CaseDocument latestRefusedConsentOrder = null;
        if (wrapper.getConsentedNotApprovedOrders() != null && !wrapper.getConsentedNotApprovedOrders().isEmpty()) {
            latestRefusedConsentOrder = wrapper.getConsentedNotApprovedOrders()
                    .get(wrapper.getConsentedNotApprovedOrders().size() - 1).getApprovedOrder().getConsentOrder();
        }
        return consentOrderNotApprovedDocumentService.getFirstOrderModifiedAfterSecondOrder(
                latestRefusedConsentOrder, latestGeneralOrder, userAuth);
    }


}
