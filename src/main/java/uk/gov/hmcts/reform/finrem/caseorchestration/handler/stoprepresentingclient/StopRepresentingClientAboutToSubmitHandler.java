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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty.getIntervenerBarristerByIndex;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.getIntervenerSolicitorByIndex;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.RESPONDENT;

@Slf4j
@Service
public class StopRepresentingClientAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private static final String UNREACHABLE_MESSAGE = "Unreachable";

    private final UpdateRepresentationWorkflowService nocWorkflowService;

    private final CaseRoleService caseRoleService;

    private final ManageBarristerService manageBarristerService;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    private final IntervenerService intervenerService;

    record StopRepresentingRequest(long caseId, FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore,
                                   boolean requestedByApplicantRep, boolean requestedByRespondentRep,
                                   boolean requestedByIntervenerRep, Optional<Integer> intervenerIndex) {}

    private static final String WARNING_MESSAGE =
        "Are you sure you wish to stop representing your client? "
            + "If you continue your access to this access will be removed";

    public StopRepresentingClientAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      UpdateRepresentationWorkflowService nocWorkflowService,
                                                      CaseRoleService caseRoleService,
                                                      ManageBarristerService manageBarristerService,
                                                      BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater,
                                                      IntervenerService intervenerService) {
        super(finremCaseDetailsMapper);
        this.caseRoleService = caseRoleService;
        this.nocWorkflowService = nocWorkflowService;
        this.manageBarristerService = manageBarristerService;
        this.barristerChangeCaseAccessUpdater = barristerChangeCaseAccessUpdater;
        this.intervenerService = intervenerService;
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
        final FinremCaseData finremCaseData = finremCaseDetails.getData();

        StopRepresentingRequest stopRepresentingRequest = buildStopRepresentingRequest(callbackRequest, userAuthorisation);
        logStopRepresentingRequest(stopRepresentingRequest);

        clearOrganisationPolicyAndRelatedBarristerSettings(stopRepresentingRequest);

        // Populating entered service address
        Pair<Address, Boolean> serviceAddressConfig = getServiceAddressConfig(finremCaseData);
        populateServiceAddressToApplicantOrRespondent(stopRepresentingRequest, serviceAddressConfig);
        populateServiceAddressToIntervener(stopRepresentingRequest, serviceAddressConfig);
        handleStopRepresentingClient(stopRepresentingRequest, userAuthorisation);
        
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .warnings(calculateWarnings(finremCaseData))
            .build();
    }

    private StopRepresentingRequest buildStopRepresentingRequest(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        FinremCaseData finremCaseData =  callbackRequest.getCaseDetails().getData();
        return new StopRepresentingRequest(
            callbackRequest.getCaseDetails().getId(),
            callbackRequest.getCaseDetails(),
            callbackRequest.getCaseDetailsBefore(),
            caseRoleService.isApplicantRepresentative(finremCaseData, userAuthorisation),
            caseRoleService.isRespondentRepresentative(finremCaseData, userAuthorisation),
            caseRoleService.isIntervenerRepresentative(finremCaseData, userAuthorisation),
            caseRoleService.getIntervenerIndex(finremCaseData, userAuthorisation)
        );
    }

    private int getIntervenerIndex(StopRepresentingRequest request) {
        return request.intervenerIndex.orElseThrow(() -> new IllegalStateException("Intervener index is missing"));
    }

    private void handleStopRepresentingClient(StopRepresentingRequest request, String userAuthorisation) {
        if (request.requestedByApplicantRep || request.requestedByRespondentRep) {
            // below also update the representative history
            nocWorkflowService.prepareNoticeOfChangeAndOrganisationPolicy(request.finremCaseDetails.getData(),
                request.finremCaseDetailsBefore.getData(), STOP_REPRESENTING_CLIENT, userAuthorisation);

            updateBarristerChangeToRepresentationUpdateHistory(request, BarristerParty.APPLICANT, userAuthorisation);
            updateBarristerChangeToRepresentationUpdateHistory(request, BarristerParty.RESPONDENT, userAuthorisation);
        } else if (request.requestedByIntervenerRep) {
            int intervenerIndex = getIntervenerIndex(request);

            // removing access from the represented intervener. i.e. only intervener1 if the requestor is on intervener 1
            intervenerService.updateIntervenerSolicitorStopRepresentingHistory(request.finremCaseDetails.getData(),
                request.finremCaseDetailsBefore.getData(), intervenerIndex, userAuthorisation);
            updateBarristerChangeToRepresentationUpdateHistory(request, getIntervenerBarristerByIndex(intervenerIndex),
                userAuthorisation);
        }
    }

    private void updateBarristerChangeToRepresentationUpdateHistory(StopRepresentingRequest request,
                                                                    BarristerParty barristerParty, String userAuthorisation) {
        barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(request.finremCaseDetails,
            manageBarristerService.getBarristerChange(request.finremCaseDetails, request.finremCaseDetailsBefore.getData(),
                barristerParty), STOP_REPRESENTING_CLIENT, userAuthorisation);
    }

    private List<String> calculateWarnings(FinremCaseData finremCaseData) {
        if (isHavingClientConsent(finremCaseData) || isHavingJudicialApproval(finremCaseData)) {
            return List.of(WARNING_MESSAGE);
        } else {
            throw new IllegalStateException(UNREACHABLE_MESSAGE);
        }
    }

    private void logStopRepresentingRequest(StopRepresentingRequest representingRequest) {
        log.info("{} - {} representative stops representing a client with a {}", representingRequest.caseId,
            describeRepresentative(representingRequest),
            describeApprovalSource(representingRequest.finremCaseDetails.getData()));
    }

    private Pair<Address, Boolean> getServiceAddressConfig(FinremCaseData finremCaseData) {
        StopRepresentationWrapper wrapper = finremCaseData.getStopRepresentationWrapper();
        return Pair.of(wrapper.getClientAddressForService(), YesOrNo.isYes(wrapper.getClientAddressForServiceConfidential()));
    }

    private void clearOrganisationPolicyAndRelatedBarristerSettings(StopRepresentingRequest stopRepresentingRequest) {
        FinremCaseData caseData = stopRepresentingRequest.finremCaseDetails.getData();

        // Set the party who is being unrepresented. null if intervener
        caseData.getContactDetailsWrapper().setNocParty(resolveNocParty(stopRepresentingRequest));

        final boolean isApplicantRepresentativeChange = stopRepresentingRequest.requestedByApplicantRep;
        final boolean isRespondentRepresentativeChange = stopRepresentingRequest.requestedByRespondentRep;
        final boolean isIntervenerRepresentativeChange = stopRepresentingRequest.requestedByIntervenerRep;

        // Determine the relevant policies and barrister lists based on the party
        OrganisationPolicy organisationPolicy = null;

        if (isApplicantRepresentativeChange || isRespondentRepresentativeChange) {
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
            }
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
        } else if (isIntervenerRepresentativeChange) {
            int intervenerIndex = getIntervenerIndex(stopRepresentingRequest);

            CaseRole intervenerCaseRole = getIntervenerSolicitorByIndex(intervenerIndex);

            var intervener = getIntervenerFromFinremCaseData(stopRepresentingRequest)
                .orElseThrow(() -> new IllegalStateException(UNREACHABLE_MESSAGE));

            organisationPolicy = intervener.getIntervenerOrganisation();
            // TODO check if the requestor org having the same org id. otherwise don't remove it
            // if
            intervener.setIntervenerOrganisation(
                getDefaultOrganisationPolicy(intervenerCaseRole)
            );
            // TODO

//            barristerCollection = caseData.getBarristerCollectionWrapper()
//                .getIntervenerBarristersByIndex(intervenerIndex);
        }
    }

    private void populateServiceAddressToApplicantOrRespondent(StopRepresentingRequest stopRepresentingRequest,
                                                               Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        ContactDetailsWrapper contactDetailsWrapper = stopRepresentingRequest.finremCaseDetails.getData()
            .getContactDetailsWrapper();

        if (stopRepresentingRequest.requestedByApplicantRep) {
            setApplicantUnrepresented(stopRepresentingRequest.finremCaseDetails.getData());
            contactDetailsWrapper.setApplicantAddress(serviceAddress);
            contactDetailsWrapper.setApplicantAddressHiddenFromRespondent(YesOrNo.forValue(isConfidential));
        } else if (stopRepresentingRequest.requestedByRespondentRep) {
            setRespondentUnrepresented(stopRepresentingRequest.finremCaseDetails.getData());
            contactDetailsWrapper.setRespondentAddress(serviceAddress);
            contactDetailsWrapper.setRespondentAddressHiddenFromApplicant(YesOrNo.forValue(isConfidential));
        }
    }

    private void populateServiceAddressToIntervener(StopRepresentingRequest stopRepresentingRequest,
                                                    Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        getIntervenerFromFinremCaseData(stopRepresentingRequest).ifPresent(intervenerWrapper -> {
            setIntervenerUnrepresented(intervenerWrapper);
            intervenerWrapper.setIntervenerAddress(serviceAddress);
            intervenerWrapper.setIntervenerAddressConfidential(YesOrNo.forValue(isConfidential));
        });
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

    private Optional<IntervenerWrapper> getIntervenerFromFinremCaseData(StopRepresentingRequest stopRepresentingRequest) {
        List<IntervenerWrapper> intervenerWrappers = stopRepresentingRequest.finremCaseDetails.getData().getInterveners();
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
            throw new IllegalStateException(UNREACHABLE_MESSAGE);
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
            throw new IllegalStateException(UNREACHABLE_MESSAGE);
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
