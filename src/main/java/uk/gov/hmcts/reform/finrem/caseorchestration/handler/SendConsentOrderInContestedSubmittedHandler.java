package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SendConsentOrderInContestedSubmittedHandler extends FinremCallbackHandler {
    private final NotificationService notificationService;
    private final GeneralOrderService generalOrderService;
    private final CcdService ccdService;

    public SendConsentOrderInContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       NotificationService notificationService,
                                                       GeneralOrderService generalOrderService,
                                                       CcdService ccdService) {
        super(finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.generalOrderService = generalOrderService;
        this.ccdService = ccdService;
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

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} submitted callback for case id: {}", callbackRequest.getEventType(), caseDetails.getId());

        List<String> parties = generalOrderService.getParties(caseDetails);
        log.info("Selected parties {} on case {}", parties, caseDetails.getId());

        sendNotifications(callbackRequest, parties);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }

    private void sendNotifications(FinremCallbackRequest callbackRequest, List<String> parties) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        String caseId = String.valueOf(caseDetails.getId());

        if (Objects.nonNull(caseData.getFinalOrderCollection())) {
            log.info("Received request to send email for 'Order Approved' for Case ID: {}", caseId);
            if (notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
                && parties.contains(CaseRole.APP_SOLICITOR.getValue())) {
                log.info("Sending 'Order Approved' email notification to Applicant Solicitor for Case ID: {}", caseId);
                notificationService.sendContestOrderApprovedEmailApplicant(caseDetails);
            }

            if (notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)
                && parties.contains(CaseRole.RESP_SOLICITOR.getValue())) {
                log.info("Sending 'Order Approved' email notification to Respondent Solicitor for Case ID: {}", caseId);
                notificationService.sendContestOrderApprovedEmailRespondent(caseDetails);
            }
            // TODO: Check applicant and respondent sending correct notifications, add notifications for interveners
        }
    }
}
