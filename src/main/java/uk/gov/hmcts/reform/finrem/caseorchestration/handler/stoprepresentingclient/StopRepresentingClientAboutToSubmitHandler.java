package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.getIntervenerSolicitorByIndex;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.RESPONDENT;

@Slf4j
@Service
public class StopRepresentingClientAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final UpdateRepresentationWorkflowService nocWorkflowService;

    private final CaseRoleService caseRoleService;

    private final ManageBarristerService manageBarristerService;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    record StopRepresentingRequest(FinremCaseData finremCaseData, boolean requestedByApplicantRep,
                                   boolean requestedByRespondentRep, boolean requestedByIntervenerRep,
                                   Optional<Integer> intervenerIndex) {}

    private static final String WARNING_MESSAGE =
        "Are you sure you wish to stop representing your client? "
            + "If you continue your access to this access will be removed";

    public StopRepresentingClientAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      UpdateRepresentationWorkflowService nocWorkflowService,
                                                      CaseRoleService caseRoleService,
                                                      ManageBarristerService manageBarristerService,
                                                      BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater) {
        super(finremCaseDetailsMapper);
        this.caseRoleService = caseRoleService;
        this.nocWorkflowService = nocWorkflowService;
        this.manageBarristerService = manageBarristerService;
        this.barristerChangeCaseAccessUpdater = barristerChangeCaseAccessUpdater;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType)
            && Arrays.asList(CONTESTED, CONSENTED).contains(caseType)
            && STOP_REPRESENTING_CLIENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        final FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        final FinremCaseDetails finremCaseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        final FinremCaseData finremCaseData = finremCaseDetails.getData();
        final FinremCaseData finremCaseDataBefore = finremCaseDetailsBefore.getData();
        final List<String> warnings = new ArrayList<>();
        StopRepresentingRequest stopRepresentingRequest = new StopRepresentingRequest(finremCaseData,
            caseRoleService.isApplicantRepresentative(finremCaseData, userAuthorisation),
            caseRoleService.isRespondentRepresentative(finremCaseData, userAuthorisation),
            caseRoleService.isIntervenerRepresentative(finremCaseData, userAuthorisation),
            caseRoleService.getIntervenerIndex(finremCaseData, userAuthorisation)
        );

        populateWarnings(finremCaseData, warnings);
        logStopRepresentingRequest(stopRepresentingRequest);
        clearOrganisationPolicyAndRelatedBarristerSettings(stopRepresentingRequest);

        Pair<Address, Boolean> serviceAddressConfig = getServiceAddressConfig(finremCaseData);
        populateServiceAddressToApplicantOrRespondent(stopRepresentingRequest, serviceAddressConfig);
        populateServiceAddressToIntervener(stopRepresentingRequest, serviceAddressConfig);

        processRepresentationChange(stopRepresentingRequest, finremCaseDetails, finremCaseDataBefore, userAuthorisation);
        
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .warnings(warnings)
            .build();
    }

    private void processRepresentationChange(StopRepresentingRequest stopRepresentingRequest,
                                             FinremCaseDetails finremCaseDetails,
                                             FinremCaseData finremCaseDataBefore,
                                             String userAuthorisation) {
        if (stopRepresentingRequest.requestedByApplicantRep || stopRepresentingRequest.requestedByRespondentRep) {
            nocWorkflowService.prepareChangeOrganisationRequestAndOrganisationPolicy(finremCaseDetails.getData(),
                finremCaseDataBefore, STOP_REPRESENTING_CLIENT, userAuthorisation);

            barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(finremCaseDetails,
                manageBarristerService.getBarristerChange(finremCaseDetails, finremCaseDataBefore, BarristerParty.APPLICANT),
                STOP_REPRESENTING_CLIENT, userAuthorisation);

            barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(finremCaseDetails,
                manageBarristerService.getBarristerChange(finremCaseDetails, finremCaseDataBefore, BarristerParty.RESPONDENT),
                STOP_REPRESENTING_CLIENT, userAuthorisation);
        } else if (stopRepresentingRequest.requestedByIntervenerRep) {
            barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(finremCaseDetails,
                manageBarristerService.getBarristerChange(finremCaseDetails, finremCaseDataBefore, BarristerParty.INTERVENER1),
                STOP_REPRESENTING_CLIENT, userAuthorisation);
            barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(finremCaseDetails,
                manageBarristerService.getBarristerChange(finremCaseDetails, finremCaseDataBefore, BarristerParty.INTERVENER2),
                STOP_REPRESENTING_CLIENT, userAuthorisation);
            barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(finremCaseDetails,
                manageBarristerService.getBarristerChange(finremCaseDetails, finremCaseDataBefore, BarristerParty.INTERVENER3),
                STOP_REPRESENTING_CLIENT, userAuthorisation);
            barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(finremCaseDetails,
                manageBarristerService.getBarristerChange(finremCaseDetails, finremCaseDataBefore, BarristerParty.INTERVENER4),
                STOP_REPRESENTING_CLIENT, userAuthorisation);
        }
    }

    private void populateWarnings(FinremCaseData finremCaseData, List<String> warnings) {
        if (isHavingClientConsent(finremCaseData) || isHavingJudicialApproval(finremCaseData)) {
            warnings.add(WARNING_MESSAGE);
        } else {
            throw new IllegalStateException("Client consent or judicial approval is required but missing.");
        }
    }

    private void logStopRepresentingRequest(StopRepresentingRequest representingRequest) {
        log.info("{} - {} representative stops representing a client with a {}", representingRequest.finremCaseData.getCcdCaseId(),
            describeRepresentative(representingRequest),
            describeApprovalSource(representingRequest.finremCaseData));
    }

    private Pair<Address, Boolean> getServiceAddressConfig(FinremCaseData finremCaseData) {
        StopRepresentationWrapper wrapper = finremCaseData.getStopRepresentationWrapper();
        return Pair.of(wrapper.getClientAddressForService(), YesOrNo.isYes(wrapper.getClientAddressForServiceConfidential()));
    }

    private void clearOrganisationPolicyAndRelatedBarristerSettings(StopRepresentingRequest stopRepresentingRequest) {
        FinremCaseData caseData = stopRepresentingRequest.finremCaseData;
        NoticeOfChangeParty noticeOfChangeParty = resolveNocParty(stopRepresentingRequest);

        // Set the party who is being unrepresented. null if intervener
        caseData.getContactDetailsWrapper().setNocParty(noticeOfChangeParty);

        final boolean isApplicantRepresentativeChange = stopRepresentingRequest.requestedByApplicantRep;
        final boolean isRespondentRepresentativeChange = stopRepresentingRequest.requestedByRespondentRep;
        final boolean isIntervenerRepresentativeChange = stopRepresentingRequest.requestedByIntervenerRep;

        // Determine the relevant policies and barrister lists based on the party
        OrganisationPolicy organisationPolicy = null;
        List<BarristerCollectionItem> barristerCollection;

        if (isApplicantRepresentativeChange) {
            organisationPolicy = caseData.getApplicantOrganisationPolicy();
            barristerCollection = caseData.getBarristerCollectionWrapper().getApplicantBarristers();
            caseData.setApplicantOrganisationPolicy(getDefaultOrganisationPolicy(CaseRole.APP_SOLICITOR));
        } else if (isRespondentRepresentativeChange) {
            organisationPolicy = caseData.getRespondentOrganisationPolicy();
            barristerCollection = caseData.getBarristerCollectionWrapper().getRespondentBarristers();
            caseData.setRespondentOrganisationPolicy(getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR));
        } else {
            barristerCollection = null;
            if (isIntervenerRepresentativeChange) {
                getTargetIntervener(stopRepresentingRequest).ifPresent(a ->
                    {
                        Integer intervenerIndex = Objects.requireNonNull(
                            stopRepresentingRequest.intervenerIndex.orElse(null),
                            "intervenerIndex must not be null"
                        );
                        a.setIntervenerOrganisation(
                            getDefaultOrganisationPolicy(getIntervenerSolicitorByIndex(intervenerIndex))
                        );
                    }
                );
                // TODO for Intv. Barristers intvr?BarristerCollection
            }
        }

        // Extract the Organisation ID to remove (centralized logic for applicant and respondent representative change only)
        ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .ifPresent(orgIdToRemove -> {
                // Remove barristers associated with the organization ID being removed
                emptyIfNull(barristerCollection).removeIf(el ->
                    ofNullable(el.getValue())
                        .map(Barrister::getOrganisation)
                        .map(Organisation::getOrganisationID)
                        .filter(orgIdToRemove::equals)
                        .isPresent()
                );
            });
    }

    private void populateServiceAddressToApplicantOrRespondent(StopRepresentingRequest stopRepresentingRequest,
                                                               Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        ContactDetailsWrapper contactDetailsWrapper = stopRepresentingRequest.finremCaseData.getContactDetailsWrapper();

        if (stopRepresentingRequest.requestedByApplicantRep) {
            setApplicantUnrepresented(stopRepresentingRequest.finremCaseData);
            contactDetailsWrapper.setApplicantAddress(serviceAddress);
            contactDetailsWrapper.setApplicantAddressHiddenFromRespondent(YesOrNo.forValue(isConfidential));
        } else if (stopRepresentingRequest.requestedByRespondentRep) {
            setRespondentUnrepresented(stopRepresentingRequest.finremCaseData);
            contactDetailsWrapper.setRespondentAddress(serviceAddress);
            contactDetailsWrapper.setRespondentAddressHiddenFromApplicant(YesOrNo.forValue(isConfidential));
        }
    }

    private void populateServiceAddressToIntervener(StopRepresentingRequest stopRepresentingRequest,
                                                    Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        getTargetIntervener(stopRepresentingRequest).ifPresent(intervenerWrapper ->
            {
                setIntervenerUnrepresented(intervenerWrapper);
                intervenerWrapper.setIntervenerAddress(serviceAddress);
                intervenerWrapper.setIntervenerAddressConfidential(YesOrNo.forValue(isConfidential));
            }
        );
    }

    private void setApplicantUnrepresented(FinremCaseData finremCaseData) {
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
    }

    private void setRespondentUnrepresented(FinremCaseData finremCaseData) {
        if (finremCaseData.isConsentedApplication()) {
            finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);
        } else {
            finremCaseData.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        }
    }

    private void setIntervenerUnrepresented(IntervenerWrapper intervenerWrapper) {
        intervenerWrapper.setIntervenerRepresented(YesOrNo.NO);
    }

    private NoticeOfChangeParty resolveNocParty(StopRepresentingRequest stopRepresentingRequest) {
        return stopRepresentingRequest.requestedByApplicantRep ? APPLICANT
            : (stopRepresentingRequest.requestedByRespondentRep ? RESPONDENT : null);
    }

    private Optional<IntervenerWrapper> getTargetIntervener(StopRepresentingRequest stopRepresentingRequest) {
        List<IntervenerWrapper> intervenerWrappers = stopRepresentingRequest.finremCaseData.getInterveners();
        return stopRepresentingRequest.intervenerIndex()
            .map(i -> i - 1) // starts with 1
            .map(intervenerWrappers::get);
    }

    private String describeApprovalSource(FinremCaseData finremCaseData) {
        if (isHavingClientConsent(finremCaseData)) {
            return "client consent";
        } else if (isHavingJudicialApproval(finremCaseData)) {
            return "judicial approval";
        } else {
            throw new IllegalStateException("Unreachable code.");
        }
    }

    private String describeRepresentative(StopRepresentingRequest representingRequest) {
        if (representingRequest.requestedByRespondentRep) {
            return "respondent";
        } else if (representingRequest.requestedByApplicantRep) {
            return "applicant";
        } else if (representingRequest.requestedByIntervenerRep) {
            return format("intervener %s", representingRequest.intervenerIndex.orElse(null));
        } else {
            throw new IllegalStateException("Unknown representation requested.");
        }
    }

    private boolean isHavingClientConsent(FinremCaseData finremCaseData) {
        return YesOrNo.isYes(finremCaseData.getStopRepresentationWrapper().getStopRepClientConsent());
    }

    private boolean isHavingJudicialApproval(FinremCaseData finremCaseData) {
        return YesOrNo.isYes(finremCaseData.getStopRepresentationWrapper().getStopRepJudicialApproval());
    }

    private OrganisationPolicy getDefaultOrganisationPolicy(CaseRole role) {
        return OrganisationPolicy
            .builder()
            .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
            .orgPolicyReference(null)
            .orgPolicyCaseAssignedRole(role.getCcdCode())
            .build();
    }
}
