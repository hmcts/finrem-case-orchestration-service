package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventWithDescription;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.LitigantRevocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    private final ManageBarristerService manageBarristerService;

    private final StopRepresentingClientCorresponder stopRepresentingClientCorresponder;

    private final FeatureToggleService featureToggleService;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final RetryExecutor retryExecutor;

    private final IntervenerService intervenerService;

    public StopRepresentingClientSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  StopRepresentingClientService stopRepresentingClientService,
                                                  StopRepresentingClientCorresponder stopRepresentingClientCorresponder,
                                                  FeatureToggleService featureToggleService,
                                                  ApplicationEventPublisher applicationEventPublisher,
                                                  RetryExecutor retryExecutor,
                                                  ManageBarristerService manageBarristerService,
                                                  IntervenerService intervenerService) {
        super(finremCaseDetailsMapper);
        this.stopRepresentingClientService = stopRepresentingClientService;
        this.featureToggleService = featureToggleService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.retryExecutor = retryExecutor;
        this.stopRepresentingClientCorresponder = stopRepresentingClientCorresponder;
        this.manageBarristerService = manageBarristerService;
        this.intervenerService = intervenerService;
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
            return submittedResponse();
        } else {
            CompletableFuture.runAsync(() ->
                revokePartiesAccessAndNotifyParties(info),
                // Add a delay to prevent a fast response so the confirmation body is not shown
                CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS)
            );
            return submittedResponse(
                toConfirmationHeader("Notice of change request submitted"),
                toConfirmationBody("Your changes will be applied shortly."));
        }
    }

    /**
     * Revokes the applicant's or respondent's solicitor and prepares related correspondence events.
     *
     * <p>This method attempts to revoke solicitor access using
     * {@link StopRepresentingClientService#revokeApplicantSolicitorOrRespondentSolicitor(StopRepresentingClientInfo)}
     * with retry support. Suppress mode is used for error handling, as third-party solicitors are unable
     * to resolve such issues. Any errors that occur will alert the IT team via the dfr_prod_alerts Slack channel.
     *
     * <p>If the revocation is successful, it performs a clean-up operation to reset
     * any organisation-related fields affected by the Notice of Change (NOC) workflow.
     *
     * <p>It then prepares and aggregates notification events, including:
     * <ul>
     *     <li>Standard litigant revocation notifications (to former representatives)</li>
     *     <li>Letter-based notification events (retrieved with retry support)</li>
     * </ul>
     *
     * <p>If revocation fails or no result is returned, an empty list is returned.
     *
     * @param info the {@link StopRepresentingClientInfo} containing case and party details
     * @return a list of {@link SendCorrespondenceEventWithDescription} representing notification events;
     *         may be empty if no actions were performed
     */
    private List<SendCorrespondenceEventWithDescription> revokeApplicantSolicitorOrRespondentSolicitor(StopRepresentingClientInfo info) {

        Optional<LitigantRevocation> litigantRevocationOptional =
            retryExecutor.supplyWithRetrySuppressException(
                () -> stopRepresentingClientService.revokeApplicantSolicitorOrRespondentSolicitor(info),
                "revoking %s access".formatted(describeLitigantPartyString(info.getFinremCaseData())),
                info.getCaseIdInString());

        List<SendCorrespondenceEventWithDescription> events = new ArrayList<>();

        litigantRevocationOptional.ifPresent(litigantRevocation -> {

            if (litigantRevocation.wasRevoked()) {
                // Call to internal service (coreCaseDataApi) to update the case data to clear ChangeOrganisationRequestField
                retryExecutor.runWithRetrySuppressException(
                    () -> stopRepresentingClientService.performCleanUpAfterNocWorkflow(info),
                    "cleaning up after noc workflow",
                    info.getCaseIdInString());
            }

            events.addAll(stopRepresentingClientCorresponder.prepareRepresentativeRevocationNotificationEvent(litigantRevocation, info));
            events.addAll(
                retryExecutor.supplyWithRetrySuppressException(
                    () -> stopRepresentingClientCorresponder.prepareLitigantRevocationLetterNotificationEvents(litigantRevocation, info),
                    "preparing litigant letter notifications",
                    info.getCaseIdInString()
                ).orElse(List.of())
            );
        });

        return events;
    }

    private List<SendCorrespondenceEventWithDescription> revokeIntervenerSolicitor(StopRepresentingClientInfo info) {
        return stopRepresentingClientService.getToBeRevokedIntervenerSolicitors(info)
            .stream()
            .flatMap(intervenerWrapper ->
                retryExecutor.supplyWithRetrySuppressException(
                    () -> {
                        intervenerService.revokeIntervenerSolicitor(info.getCaseId(), intervenerWrapper);
                        return stopRepresentingClientCorresponder
                            .prepareIntervenerSolicitorEmailNotificationEvent(info, intervenerWrapper.getIntervenerType());
                    },
                    "revoking %s access".formatted(describeIntervener(intervenerWrapper)),
                    info.getCaseIdInString()
                ).stream()
            )
            .toList();
    }

    protected void revokePartiesAccessAndNotifyParties(StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventWithDescription> events = new ArrayList<>();
        events.addAll(revokeApplicantSolicitorOrRespondentSolicitor(info));
        events.addAll(revokeIntervenerSolicitor(info));
        events.addAll(revokeBarristers(info));

        log.info("{} - about to send {} notifications to relevant parties", info.getCaseId(), events.size());
        events.forEach(event ->
            retryExecutor.runWithRetrySuppressException(() -> applicationEventPublisher.publishEvent(event.getEvent()),
                event.getDescription(), info.getCaseIdInString())
        );
    }

    private String describeIntervener(IntervenerWrapper intervenerWrapper) {
        return ofNullable(intervenerWrapper.getIntervenerType()).map(IntervenerType::getTypeValue)
            .orElse("(intervener#unknown)");
    }

    private String describeLitigantPartyString(FinremCaseData finremCaseData) {
        String party = isApplicantForRepresentationChange(finremCaseData) ? "applicant" : "";
        party = (isBlank(party) && isRespondentForRepresentationChange(finremCaseData)) ? "respondent" : party;
        return party;
    }

    private BarristerChange getToBeRevokedBarristers(StopRepresentingClientInfo info, BarristerParty barristerParty) {
        return manageBarristerService.getBarristerChange(info.getCaseDetails(), info.getFinremCaseDataBefore(),
            barristerParty);
    }

    private List<SendCorrespondenceEventWithDescription> revokeBarristers(StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventWithDescription> events = new ArrayList<>();

        for (BarristerParty party : BarristerParty.values()) {
            BarristerChange barristerChange = getToBeRevokedBarristers(info, party);
            Set<Barrister> removed = Optional.ofNullable(barristerChange)
                .orElseThrow(() -> new IllegalStateException("barristerChange must not be null"))
                .getRemoved();

            if (CollectionUtils.isEmpty(removed)) {
                continue;
            }

            String description = "revoking " + party.name().toLowerCase() + " barrister access";

            Optional<List<SendCorrespondenceEventWithDescription>> maybeResult =
                retryExecutor.supplyWithRetrySuppressException(
                    () -> revokeBarristers(info, barristerChange),
                    description,
                    info.getCaseIdInString()
                );

            maybeResult.ifPresent(events::addAll);
        }

        return events;
    }

    private List<SendCorrespondenceEventWithDescription> revokeBarristers(StopRepresentingClientInfo info, BarristerChange barristerChange) {
        manageBarristerService.executeBarristerChange(info.getCaseId(), barristerChange);

        BarristerParty barristerParty = barristerChange.getBarristerParty();
        return SetUtils.emptyIfNull(barristerChange.getRemoved())
            .stream()
            .map(barrister -> switch (barristerParty) {
                case APPLICANT -> stopRepresentingClientCorresponder
                    .prepareApplicantBarristerEmailNotificationEvent(info, barrister);
                case RESPONDENT -> stopRepresentingClientCorresponder
                    .prepareRespondentBarristerEmailNotificationEvent(info, barrister);
                case INTERVENER1 -> stopRepresentingClientCorresponder
                    .prepareIntervenerBarristerEmailNotificationEvent(info, IntervenerType.INTERVENER_ONE, barrister);
                case INTERVENER2 -> stopRepresentingClientCorresponder
                    .prepareIntervenerBarristerEmailNotificationEvent(info, IntervenerType.INTERVENER_TWO, barrister);
                case INTERVENER3 -> stopRepresentingClientCorresponder
                    .prepareIntervenerBarristerEmailNotificationEvent(info, IntervenerType.INTERVENER_THREE, barrister);
                case INTERVENER4 -> stopRepresentingClientCorresponder
                    .prepareIntervenerBarristerEmailNotificationEvent(info, IntervenerType.INTERVENER_FOUR, barrister);
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
