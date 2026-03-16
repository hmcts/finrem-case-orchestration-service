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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
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

        Optional<StopRepresentingClientService.LitigantRevocation> litigantRevocationOptional =
            retryExecutor.supplyWithRetrySuppressException(
                () -> stopRepresentingClientService.revokeApplicantSolicitorOrRespondentSolicitor(info),
                "revoking %s access".formatted(describeLigtantPartyString(info.getFinremCaseData())),
                info.getCaseIdInString());

        List<SendCorrespondenceEventEnvelop> ret = new ArrayList<>();

        litigantRevocationOptional.ifPresent(litigantRevocation -> {

            if (litigantRevocation.wasRevoked()) {
                // Call to internal service (coreCaseDataApi) to update the case data to clear ChangeOrganisationRequestField
                retryExecutor.runWithRetrySuppressException(
                    () -> stopRepresentingClientService.performCleanUpAfterNocWorkflow(info),
                    "cleaning up after noc workflow",
                    info.getCaseIdInString());
            }

            ret.addAll(stopRepresentingClientService.prepareLitigantRevocationNotificationEvents(litigantRevocation, info));
            ret.addAll(
                retryExecutor.supplyWithRetrySuppressException(
                    () -> stopRepresentingClientService.prepareLitigantRevocationLetterNotificationEvents(litigantRevocation, info),
                    "preparing litigant letter notifications",
                    info.getCaseIdInString()
                ).orElse(List.of())
            );
        });

        return ret;
    }

    private List<SendCorrespondenceEventEnvelop> revokeIntervenerSolicitor(StopRepresentingClientInfo info) {
        return stopRepresentingClientService.getToBeRevokedIntervenerSolicitors(info)
            .stream()
            .flatMap(intervenerWrapper ->
                retryExecutor.supplyWithRetrySuppressException(
                    () -> stopRepresentingClientService.revokeIntervenerSolicitor(info, intervenerWrapper),
                    "revoking %s access".formatted(describeIntervener(intervenerWrapper)),
                    info.getCaseIdInString()
                ).stream()
            )
            .toList();
    }

    private List<SendCorrespondenceEventEnvelop> revokeBarristers(StopRepresentingClientInfo info) {
        return Stream.of(
                BarristerParty.APPLICANT,
                BarristerParty.RESPONDENT,
                BarristerParty.INTERVENER1,
                BarristerParty.INTERVENER2,
                BarristerParty.INTERVENER3,
                BarristerParty.INTERVENER4
            )
            .map(party -> Map.entry(party, stopRepresentingClientService.getToBeRevokedBarristers(info, party)))
            .filter(entry -> entry.getValue() != null && !CollectionUtils.isEmpty(entry.getValue().getRemoved()))
            .flatMap(entry ->
                retryExecutor.supplyWithRetrySuppressException(
                    () -> stopRepresentingClientService.revokeBarristers(info, entry.getValue()),
                    "revoking " + entry.getKey().name().toLowerCase() + " barrister access",
                    info.getCaseIdInString()
                ).stream()
            )
            .flatMap(List::stream)
            .toList();
    }

    private void revokePartiesAccessAndNotifyParties(StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventEnvelop> envelops = new ArrayList<>();
        envelops.addAll(revokeApplicantSolicitorOrRespondentSolicitor(info));
        envelops.addAll(revokeIntervenerSolicitor(info));
        envelops.addAll(revokeBarristers(info));

        log.info("{} - about to send {} notifications to relevant parties", info.getCaseId(), envelops.size());
        // publish all notification
        envelops.forEach(envelop ->
            retryExecutor.runWithRetrySuppressException(() -> applicationEventPublisher.publishEvent(envelop.getEvent()),
                envelop.getDescription(), info.getCaseIdInString())
        );
    }

    private String describeIntervener(IntervenerWrapper intervenerWrapper) {
        return ofNullable(intervenerWrapper.getIntervenerType()).map(IntervenerType::getTypeValue)
            .orElse("(intervener#unknown)");
    }

    private String describeLigtantPartyString(FinremCaseData finremCaseData) {
        String party = isApplicantForRepresentationChange(finremCaseData) ? "applicant" : "";
        party = (isBlank(party) && isRespondentForRepresentationChange(finremCaseData)) ? "respondent" : party;
        return party;
    }
}
