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

        StopRepresentingRequest request = buildStopRepresentingRequest(callbackRequest, userAuthorisation);
        logStopRepresentingRequest(request);

        processRequest(request);

        // Populating entered service address
        populateServiceAddressToParty(request);
        buildRepresentationUpdateHistoryAndNocDataIfAny(request, userAuthorisation);
        
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

    private void buildRepresentationUpdateHistoryAndNocDataIfAny(StopRepresentingRequest request, String userAuthorisation) {
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

    private void updateBarristerChangeToRepresentationUpdateHistory(StopRepresentingRequest request, BarristerParty barristerParty,
                                                                    String userAuthorisation) {
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

    private void logStopRepresentingRequest(StopRepresentingRequest request) {
        log.info("{} - {} representative stops representing a client with a {}", request.caseId,
            describeRepresentative(request),
            describeApprovalSource(request.finremCaseDetails.getData()));
    }

    private Pair<Address, Boolean> getServiceAddressConfig(FinremCaseData finremCaseData) {
        StopRepresentationWrapper wrapper = finremCaseData.getStopRepresentationWrapper();
        return Pair.of(wrapper.getClientAddressForService(), YesOrNo.isYes(wrapper.getClientAddressForServiceConfidential()));
    }

    private void removeBarristerOrganisationsMatchingOrgId(List<BarristerCollectionItem> barristerCollection, String organisationID) {
        ofNullable(organisationID).ifPresent(organisationId ->
            emptyIfNull(barristerCollection).removeIf(el ->
                ofNullable(el.getValue())
                    .map(Barrister::getOrganisation)
                    .map(Organisation::getOrganisationID)
                    .filter(organisationId::equals)
                    .isPresent()
        ));
    }

    private void removeIntervenerBarristerOrganisationsMatchingOrgId(FinremCaseData finremCaseData, String targetOrgId) {
        for (int index = 1; index <= 4; index++) { // clear all barristers with the same org ID
            removeBarristerOrganisationsMatchingOrgId(getIntervenerBarristerCollection(finremCaseData, index), targetOrgId);
        }
    }

    private List<BarristerCollectionItem> getApplicantBarristerCollection(FinremCaseData finremCaseData) {
        return finremCaseData.getBarristerCollectionWrapper().getApplicantBarristers();
    }

    private List<BarristerCollectionItem> getRespondentBarristerCollection(FinremCaseData finremCaseData) {
        return finremCaseData.getBarristerCollectionWrapper().getRespondentBarristers();
    }

    private List<BarristerCollectionItem> getIntervenerBarristerCollection(FinremCaseData finremCaseData, int index) {
        return finremCaseData.getBarristerCollectionWrapper().getIntervenerBarristersByIndex(index);
    }

    private void stopRepresentingByApplicantRepresentative(FinremCaseData finremCaseData) {
        stopRepresentingByApplicantOrRespondentRepresentative(finremCaseData, true);
    }

    private void stopRepresentingByRespondentRepresentative(FinremCaseData finremCaseData) {
        stopRepresentingByApplicantOrRespondentRepresentative(finremCaseData, false);
    }

    private void stopRepresentingByApplicantOrRespondentRepresentative(FinremCaseData finremCaseData, boolean applicant) {
        // Resetting applicant or respondent organisation policy
        OrganisationPolicy organisationPolicy = applicant
            ? finremCaseData.getApplicantOrganisationPolicy()
            : finremCaseData.getRespondentOrganisationPolicy();
        String targetOrgId = ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID).orElse(null);

        if (applicant) {
            setApplicantUnrepresented(finremCaseData);
        } else {
            setRespondentUnrepresented(finremCaseData);
        }

        removeBarristerOrganisationsMatchingOrgId(applicant
            ? getApplicantBarristerCollection(finremCaseData)
            : getRespondentBarristerCollection(finremCaseData), targetOrgId);
        removeIntervenerBarristerOrganisationsMatchingOrgId(finremCaseData, targetOrgId);

        // TODO clear all intervener solicitor with the same org ID
    }

    private void stopRepresentingByIntervenerRepresentative(FinremCaseData finremCaseData,
                                                            IntervenerWrapper intervenerWrapper) {
        OrganisationPolicy organisationPolicy = intervenerWrapper.getIntervenerOrganisation();
        String targetOrgId = ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID).orElse(null);

        setIntervenerUnrepresented(intervenerWrapper);

        removeIntervenerBarristerOrganisationsMatchingOrgId(finremCaseData, targetOrgId);
        // TODO clear all intervener solicitor with the same org ID
        // TODO unpresent same org applicant if any
        // TODO unpresent same org respondent if any
    }

    private void processRequest(StopRepresentingRequest request) {
        FinremCaseData finremCaseData = request.finremCaseDetails.getData();

        // Set the party who is being unrepresented. null if intervener
        finremCaseData.getContactDetailsWrapper().setNocParty(resolveNocParty(request));

        if (request.requestedByApplicantRep) {
            stopRepresentingByApplicantRepresentative(finremCaseData);
        } else if (request.requestedByRespondentRep) {
            stopRepresentingByRespondentRepresentative(finremCaseData);
        } else if (request.requestedByIntervenerRep) {
            stopRepresentingByIntervenerRepresentative(finremCaseData, getIntervenerFromFinremCaseData(request));
        }
    }

    private void populateServiceAddressToParty(StopRepresentingRequest request) {
        Pair<Address, Boolean> serviceAddressConfig = getServiceAddressConfig(request.finremCaseDetails.getData());
        if (request.requestedByApplicantRep || request.requestedByRespondentRep) {
            populateServiceAddressToApplicantOrRespondent(request, serviceAddressConfig);
        } else if (request.requestedByIntervenerRep) {
            populateServiceAddressToIntervener(request, serviceAddressConfig);
        }
    }

    private void populateServiceAddressToApplicantOrRespondent(StopRepresentingRequest request,
                                                               Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        ContactDetailsWrapper contactDetailsWrapper = request.finremCaseDetails.getData()
            .getContactDetailsWrapper();

        if (request.requestedByApplicantRep) {
            contactDetailsWrapper.setApplicantAddress(serviceAddress);
            contactDetailsWrapper.setApplicantAddressHiddenFromRespondent(YesOrNo.forValue(isConfidential));
        } else if (request.requestedByRespondentRep) {
            contactDetailsWrapper.setRespondentAddress(serviceAddress);
            contactDetailsWrapper.setRespondentAddressHiddenFromApplicant(YesOrNo.forValue(isConfidential));
        }
    }

    private void populateServiceAddressToIntervener(StopRepresentingRequest request,
                                                    Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        IntervenerWrapper intervenerWrapper = getIntervenerFromFinremCaseData(request);
        intervenerWrapper.setIntervenerAddress(serviceAddress);
        intervenerWrapper.setIntervenerAddressConfidential(YesOrNo.forValue(isConfidential));
    }

    private void setApplicantUnrepresented(FinremCaseData finremCaseData) {
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        finremCaseData.setApplicantOrganisationPolicy(getDefaultOrganisationPolicy(CaseRole.APP_SOLICITOR));
    }

    private void setRespondentUnrepresented(FinremCaseData finremCaseData) {
        if (finremCaseData.isConsentedApplication()) {
            finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);
        } else {
            finremCaseData.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        }
        finremCaseData.setRespondentOrganisationPolicy(getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR));
    }

    private void setIntervenerUnrepresented(IntervenerWrapper intervenerWrapper) {
        intervenerWrapper.setIntervenerRepresented(YesOrNo.NO);
        intervenerWrapper.setIntervenerOrganisation(getDefaultOrganisationPolicy(
            intervenerWrapper.getIntervenerSolicitorCaseRole()
        ));
    }

    private NoticeOfChangeParty resolveNocParty(StopRepresentingRequest request) {
        return request.requestedByApplicantRep ? APPLICANT
            : (request.requestedByRespondentRep ? RESPONDENT : null);
    }

    private IntervenerWrapper getIntervenerFromFinremCaseData(StopRepresentingRequest request) {
        List<IntervenerWrapper> intervenerWrappers = request.finremCaseDetails.getData().getInterveners();
        return request.intervenerIndex()
            .map(i -> i - 1) // starts with 1
            .map(intervenerWrappers::get).orElseThrow(IllegalStateException::new);
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

    private String describeRepresentative(StopRepresentingRequest request) {
        if (request.requestedByRespondentRep) {
            return "respondent";
        } else if (request.requestedByApplicantRep) {
            return "applicant";
        } else if (request.requestedByIntervenerRep) {
            return format("intervener %s", request.intervenerIndex.orElse(null));
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
