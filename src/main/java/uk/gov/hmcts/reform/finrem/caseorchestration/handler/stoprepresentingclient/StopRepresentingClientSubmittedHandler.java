package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventEnvelop;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isRespondentForRepresentationChange;

@Slf4j
@Service
public class StopRepresentingClientSubmittedHandler extends FinremCallbackHandler {

    private final StopRepresentingClientService stopRepresentingClientService;

    private final FeatureToggleService featureToggleService;

    private static final String CONFIRMATION_HEADER = "# Notice of change request submitted";

    private final ApplicationEventPublisher applicationEventPublisher;

    public StopRepresentingClientSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  StopRepresentingClientService stopRepresentingClientService,
                                                  FeatureToggleService featureToggleService,
                                                  ApplicationEventPublisher applicationEventPublisher) {
        super(finremCaseDetailsMapper);
        this.stopRepresentingClientService = stopRepresentingClientService;
        this.featureToggleService = featureToggleService;
        this.applicationEventPublisher = applicationEventPublisher;
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

        StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
            .userAuthorisation(userAuthorisation)
            .caseDetails(callbackRequest.getCaseDetails())
            .caseDetailsBefore(callbackRequest.getCaseDetailsBefore())
            .build();

        if (featureToggleService.isExui3990WorkaroundEnabled()) {
            revokePartiesAccessAndNotifyParties(info);
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

        return submittedResponse(CONFIRMATION_HEADER, toConfirmationBody("Your changes will be applied shortly."));
    }

    private List<SendCorrespondenceEventEnvelop> revokeApplicantSolicitorOrRespondentSolicitor(StopRepresentingClientInfo info) {
        StopRepresentingClientService.LitigantRevocation litigantRevocation = executeWithRetrySuppressingException(log,
            () -> stopRepresentingClientService.revokeApplicantSolicitorOrRespondentSolicitor(info),
            getCaseId(info), "revoking %s access".formatted(getLigtantPartyString(getFinremCaseData(info))));
        if (litigantRevocation != null) {
            // - continue sending if revocation is not null
            // - null revocation means exception may occur
            return emptyIfNull(executeWithRetrySuppressingException(log,
                () -> stopRepresentingClientService.prepareLitigantNotifications(litigantRevocation, info),
                getCaseId(info), "preparing litigant notifications"));
        }
        return List.of();
    }

    private List<SendCorrespondenceEventEnvelop> revokeIntervenerSolicitor(StopRepresentingClientInfo info) {
        return emptyIfNull(executeWithRetrySuppressingException(log,
            () -> stopRepresentingClientService.revokeIntervenerSolicitor(info),
            getCaseId(info), "revoking intervener access"));
    }

    private List<SendCorrespondenceEventEnvelop> revokeDifferentPartiesBarristers(StopRepresentingClientInfo info) {
        return emptyIfNull(executeWithRetrySuppressingException(log,
            () -> stopRepresentingClientService.revokeAllPartiesBarrister(info),
            getCaseId(info), "revoking different parties barristers' access"));
    }

    private void revokePartiesAccessAndNotifyParties(StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventEnvelop> envelops = new ArrayList<>();
        envelops.addAll(revokeApplicantSolicitorOrRespondentSolicitor(info));
        envelops.addAll(revokeIntervenerSolicitor(info));
        envelops.addAll(revokeDifferentPartiesBarristers(info));

        // publish all notification
        envelops.forEach(envelop ->
            executeWithRetrySuppressingException(log, () -> applicationEventPublisher.publishEvent(envelop.getEvent()),
                getCaseId(info), envelop.getDescription())
        );
    }

    private String getLigtantPartyString(FinremCaseData finremCaseData) {
        String party = isApplicantForRepresentationChange(finremCaseData) ? "applicant" : "";
        party = (isBlank(party) && isRespondentForRepresentationChange(finremCaseData)) ? "respondent" : party;
        return party;
    }

    private String getCaseId(StopRepresentingClientInfo info) {
        return info.getCaseDetails().getCaseIdAsString();
    }

    private FinremCaseData getFinremCaseData(StopRepresentingClientInfo info) {
        return info.getCaseDetails().getData();
    }
}
