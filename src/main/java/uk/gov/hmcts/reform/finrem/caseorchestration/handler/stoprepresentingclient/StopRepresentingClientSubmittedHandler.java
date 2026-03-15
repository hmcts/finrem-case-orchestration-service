package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventEnvelop;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.ofNullable;
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

    private final ApplicationEventPublisher applicationEventPublisher;

    private final RetryExecutor retryExecutor;

    public StopRepresentingClientSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  StopRepresentingClientService stopRepresentingClientService,
                                                  FeatureToggleService featureToggleService,
                                                  ApplicationEventPublisher applicationEventPublisher,
                                                  RetryExecutor retryExecutor) {
        super(finremCaseDetailsMapper);
        this.stopRepresentingClientService = stopRepresentingClientService;
        this.featureToggleService = featureToggleService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.retryExecutor = retryExecutor;
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
                revokePartiesAccessAndNotifyParties(info),
                // Add a delay to prevent a fast response so the confirmation body is not shown
                CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS)
            );
        }

        return submittedResponse(
            toConfirmationHeader("Notice of change request submitted"),
            toConfirmationBody("Your changes will be applied shortly."));
    }

    private List<SendCorrespondenceEventEnvelop> revokeApplicantSolicitorOrRespondentSolicitor(StopRepresentingClientInfo info) {
        StopRepresentingClientService.LitigantRevocation litigantRevocation = retryExecutor.runWithRetry(
            () -> stopRepresentingClientService.revokeApplicantSolicitorOrRespondentSolicitor(info),
            "revoking %s access".formatted(getLigtantPartyString(getFinremCaseData(info))),
            getCaseId(info));

        if (litigantRevocation != null) {
            // - continue sending if revocation is not null
            // - null revocation means exception may occur
            if (litigantRevocation.wasRevoked()) {
                executeWithRetrySuppressingException(log,
                    () -> stopRepresentingClientService.performCleanUpAfterNocWorkflow(info),
                    getCaseId(info), "cleaning up after noc workflow ");
            }

            List<SendCorrespondenceEventEnvelop> ret = new ArrayList<>();
            ret.addAll(emptyIfNull(executeWithRetrySuppressingException(log,
                () -> stopRepresentingClientService.prepareLitigantRevocationNotificationEvents(litigantRevocation, info),
                getCaseId(info), "preparing litigant notifications")));
            ret.addAll(emptyIfNull(executeWithRetrySuppressingException(log,
                () -> stopRepresentingClientService.prepareLitigantRevocationLetterNotificationEvents(litigantRevocation, info),
                getCaseId(info), "preparing litigant letter notifications")));
            return ret;
        }
        return List.of();
    }

    private List<SendCorrespondenceEventEnvelop> revokeIntervenerSolicitor(StopRepresentingClientInfo info) {
        List<IntervenerWrapper> intervenerWrappers = stopRepresentingClientService.getToBeRevokedIntervenerSolicitors(info);
        return intervenerWrappers.stream().map(intervenerWrapper ->
            executeWithRetrySuppressingException(log,
                () -> stopRepresentingClientService.revokeIntervenerSolicitor(info, intervenerWrapper),
                getCaseId(info), "revoking %s access".formatted(ofNullable(intervenerWrapper.getIntervenerType())
                    .map(IntervenerType::getTypeValue).orElse("(intervener#unknown)")))
        ).toList();
    }

    private List<SendCorrespondenceEventEnvelop> revokeBarristers(StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventEnvelop> ret = new ArrayList<>();

        for (BarristerParty party : List.of(
            BarristerParty.APPLICANT, BarristerParty.RESPONDENT,
            BarristerParty.INTERVENER1, BarristerParty.INTERVENER2, BarristerParty.INTERVENER3, BarristerParty.INTERVENER4
        )) {
            BarristerChange change = stopRepresentingClientService.getToBeRevokedBarristers(info, party);
            if (change != null && !CollectionUtils.isEmpty(change.getRemoved())) {
                List<SendCorrespondenceEventEnvelop> events = executeWithRetrySuppressingException(
                    log,
                    () -> stopRepresentingClientService.revokeBarristers(info, change),
                    getCaseId(info),
                    "revoking " + party.name().toLowerCase() + " barrister access"
                );
                if (events != null) {
                    ret.addAll(events);
                }
            }
        }
        return ret;
    }

    private void revokePartiesAccessAndNotifyParties(StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventEnvelop> envelops = new ArrayList<>();
        envelops.addAll(revokeApplicantSolicitorOrRespondentSolicitor(info));
        envelops.addAll(revokeIntervenerSolicitor(info));
        envelops.addAll(revokeBarristers(info));

        log.info("{} - about to send {} notifications to relevant parties", getCaseId(info), envelops.size());
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
