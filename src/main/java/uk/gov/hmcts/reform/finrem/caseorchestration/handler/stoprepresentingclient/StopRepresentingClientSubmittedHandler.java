package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class StopRepresentingClientSubmittedHandler extends FinremCallbackHandler {

    private final StopRepresentingClientService stopRepresentingClientService;

    private final FeatureToggleService featureToggleService;

    private static final String CONFIRMATION_HEADER = "# Notice of change request submitted";

    public StopRepresentingClientSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  StopRepresentingClientService stopRepresentingClientService,
                                                  FeatureToggleService featureToggleService) {
        super(finremCaseDetailsMapper);
        this.stopRepresentingClientService = stopRepresentingClientService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return SUBMITTED.equals(callbackType)
            && Arrays.asList(CONTESTED, CONSENTED).contains(caseType)
            && STOP_REPRESENTING_CLIENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        if (featureToggleService.isExui3990WorkaroundEnabled()) {
            stopRepresentingClientService.revokePartiesAccessAndNotifyParties(
                StopRepresentingClientInfo.builder()
                    .userAuthorisation(userAuthorisation)
                    .caseDetails(callbackRequest.getCaseDetails())
                    .caseDetailsBefore(callbackRequest.getCaseDetailsBefore())
                    .build());
        } else {
            CompletableFuture.runAsync(() ->
                stopRepresentingClientService.revokePartiesAccessAndNotifyParties(
                    StopRepresentingClientInfo.builder()
                        .userAuthorisation(userAuthorisation)
                        .caseDetails(callbackRequest.getCaseDetails())
                        .caseDetailsBefore(callbackRequest.getCaseDetailsBefore())
                        .build()),
                // Add a delay to prevent a fast response so the confirmation body is not shown
                CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS)
            );
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody("<br /><br />")
            .build();
    }
}
