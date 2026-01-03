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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.Representation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.RESPONDENT;

@Slf4j
@Service
public class StopRepresentingClientAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private static final String UNREACHABLE_MESSAGE = "Unreachable";

    private final UpdateRepresentationWorkflowService nocWorkflowService;

    private final ManageBarristerService manageBarristerService;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    private final IntervenerService intervenerService;

    private final StopRepresentingClientService stopRepresentingClientService;

    record StopRepresentingRequest(long caseId, FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore,
                                   Representation representation) {}

    private static final String WARNING_MESSAGE =
        "Are you sure you wish to stop representing your client? "
            + "If you continue your access to this access will be removed";

    private static boolean isRepresentingApplicant(StopRepresentingRequest request) {
        return request.representation.isRepresentingApplicant();
    }

    private static boolean isRepresentingRespondent(StopRepresentingRequest request) {
        return request.representation.isRepresentingRespondent();
    }

    private static boolean isRepresentingAnyInterveners(StopRepresentingRequest request) {
        return request.representation.isRepresentingAnyInterveners();
    }

    private static boolean isRepresentingAnyIntervenerBarristers(StopRepresentingRequest request) {
        return request.representation.isRepresentingAnyIntervenerBarristers();
    }

    private static String getUserId(StopRepresentingRequest request) {
        return request.representation.userId();
    }

    public StopRepresentingClientAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      UpdateRepresentationWorkflowService nocWorkflowService,
                                                      ManageBarristerService manageBarristerService,
                                                      BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater,
                                                      IntervenerService intervenerService,
                                                      StopRepresentingClientService stopRepresentingClientService) {
        super(finremCaseDetailsMapper);
        this.nocWorkflowService = nocWorkflowService;
        this.manageBarristerService = manageBarristerService;
        this.barristerChangeCaseAccessUpdater = barristerChangeCaseAccessUpdater;
        this.intervenerService = intervenerService;
        this.stopRepresentingClientService = stopRepresentingClientService;
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
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();

        return new StopRepresentingRequest(
            callbackRequest.getCaseDetails().getId(),
            callbackRequest.getCaseDetails(),
            callbackRequest.getCaseDetailsBefore(),
            stopRepresentingClientService.buildRepresentation(finremCaseData, userAuthorisation)
        );
    }

    private int getIntervenerIndex(StopRepresentingRequest request) {
        return of(request.representation.intervenerIndex()).orElseThrow(()
            -> new IllegalStateException(format("%s - expecting intervener index exists", request.caseId)));
    }

    private void buildRepresentationUpdateHistoryAndNocDataIfAny(StopRepresentingRequest request, String userAuthorisation) {
        if (isRepresentingApplicant(request) || isRepresentingRespondent(request)) {
            // below also update the representative history
            nocWorkflowService.prepareNoticeOfChangeAndOrganisationPolicy(request.finremCaseDetails.getData(),
                request.finremCaseDetailsBefore.getData(), STOP_REPRESENTING_CLIENT, userAuthorisation);

            // TODO update history for applicant/respondent/other interveners if any
        } else if (isRepresentingAnyInterveners(request)) {
            int intervenerIndex = getIntervenerIndex(request);
            intervenerService.updateIntervenerSolicitorStopRepresentingHistory(request.finremCaseDetails.getData(),
                request.finremCaseDetailsBefore.getData(), intervenerIndex, userAuthorisation);

            // TODO update history for applicant/respondent/other interveners if any
        }

        updateBarristerChangeToRepresentationUpdateHistory(request, BarristerParty.APPLICANT, userAuthorisation);
        updateBarristerChangeToRepresentationUpdateHistory(request, BarristerParty.RESPONDENT, userAuthorisation);
        updateBarristerChangeToRepresentationUpdateHistory(request, BarristerParty.INTERVENER1, userAuthorisation);
        updateBarristerChangeToRepresentationUpdateHistory(request, BarristerParty.INTERVENER2, userAuthorisation);
        updateBarristerChangeToRepresentationUpdateHistory(request, BarristerParty.INTERVENER3, userAuthorisation);
        updateBarristerChangeToRepresentationUpdateHistory(request, BarristerParty.INTERVENER4, userAuthorisation);
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

    private void removeBarristerOrganisationsMatchingOrgId(List<BarristerCollectionItem> barristerCollection, Organisation targetOrg) {
        ofNullable(targetOrg).map(Organisation::getOrganisationID).ifPresent(target ->
            emptyIfNull(barristerCollection).removeIf(el ->
                ofNullable(el.getValue())
                    .filter(barrister -> doesMatchOrganisation(targetOrg, barrister))
                    .isPresent()
        ));
    }

    private void removeIntervenerBarristerOrganisationsMatchingOrgId(FinremCaseData finremCaseData, Organisation targetOrg) {
        for (int index = 1; index <= 4; index++) { // clear all barristers with the same org ID
            removeBarristerOrganisationsMatchingOrgId(getIntervenerBarristerCollection(finremCaseData, index), targetOrg);
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

    private void stopRepresentingByApplicantOrRespondentRepresentative(FinremCaseData finremCaseData, boolean isApplicant) {
        // Resetting applicant or respondent organisation policy
        OrganisationPolicy organisationPolicy = isApplicant ? finremCaseData.getApplicantOrganisationPolicy()
            : finremCaseData.getRespondentOrganisationPolicy();
        Organisation targetOrg = ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);

        // Set parties unrepresented
        if (isApplicant) {
            setApplicantUnrepresented(finremCaseData);
        } else {
            setRespondentUnrepresented(finremCaseData);
        }
        setIntervenerUnrepresentedIfOrgMatch(finremCaseData, targetOrg);

        // Remove matching barrister organisation if any
        removeBarristerOrganisationsMatchingOrgId(
            // The applicant solicitor’s organisation must not be the same as the respondent solicitor’s.
            // Therefore, we only remove the barrister from one side.
            isApplicant
                ? getApplicantBarristerCollection(finremCaseData)
                : getRespondentBarristerCollection(finremCaseData), targetOrg);
        removeIntervenerBarristerOrganisationsMatchingOrgId(finremCaseData, targetOrg);
    }

    private void stopRepresentingByIntervenerBarrister(FinremCaseData finremCaseData, Barrister barrister) {
        Organisation targetOrg = barrister.getOrganisation();

        setApplicantUnrepresentedIfOrgMatch(finremCaseData, targetOrg);
        setRespondentUnrepresentedIfOrgMatch(finremCaseData, targetOrg);
        setIntervenerUnrepresentedIfOrgMatch(finremCaseData, targetOrg);

        removeBarristerOrganisationsMatchingOrgId(getApplicantBarristerCollection(finremCaseData), targetOrg);
        removeBarristerOrganisationsMatchingOrgId(getRespondentBarristerCollection(finremCaseData), targetOrg);
        removeIntervenerBarristerOrganisationsMatchingOrgId(finremCaseData, targetOrg);
    }

    private void stopRepresentingByIntervenerSolicitor(FinremCaseData finremCaseData, IntervenerWrapper intervenerWrapper) {
        OrganisationPolicy organisationPolicy = intervenerWrapper.getIntervenerOrganisation();
        Organisation targetOrg = ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);

        setIntervenerUnrepresented(intervenerWrapper);

        setApplicantUnrepresentedIfOrgMatch(finremCaseData, targetOrg);
        setRespondentUnrepresentedIfOrgMatch(finremCaseData, targetOrg);
        setIntervenerUnrepresentedIfOrgMatch(finremCaseData, targetOrg, intervenerWrapper);

        removeBarristerOrganisationsMatchingOrgId(getApplicantBarristerCollection(finremCaseData), targetOrg);
        removeBarristerOrganisationsMatchingOrgId(getRespondentBarristerCollection(finremCaseData), targetOrg);
        removeIntervenerBarristerOrganisationsMatchingOrgId(finremCaseData, targetOrg);
    }

    private void processRequest(StopRepresentingRequest request) {
        FinremCaseData finremCaseData = request.finremCaseDetails.getData();

        // Set the party who is being unrepresented. null if intervener
        finremCaseData.getContactDetailsWrapper().setNocParty(resolveNocParty(request));

        if (isRepresentingApplicant(request)) {
            stopRepresentingByApplicantRepresentative(finremCaseData);
        } else if (isRepresentingRespondent(request)) {
            stopRepresentingByRespondentRepresentative(finremCaseData);
        } else if (isRepresentingAnyInterveners(request)) {
            if (isRepresentingAnyIntervenerBarristers(request)) {
                stopRepresentingByIntervenerBarrister(finremCaseData, getIntervenerBarristerFromFinremCaseData(request));
            } else {
                // it must be intervener solicitor
                stopRepresentingByIntervenerSolicitor(finremCaseData, getIntervenerWrapperFromFinremCaseData(request));
            }
        }
    }

    private void populateServiceAddressToParty(StopRepresentingRequest request) {
        Pair<Address, Boolean> serviceAddressConfig = getServiceAddressConfig(request.finremCaseDetails.getData());
        if (isRepresentingApplicant(request) || isRepresentingRespondent(request)) {
            populateServiceAddressToApplicantOrRespondent(request, serviceAddressConfig);
        } else if (isRepresentingAnyInterveners(request)) {
            populateServiceAddressToIntervener(request, serviceAddressConfig);
        }
    }

    private void populateServiceAddressToApplicantOrRespondent(StopRepresentingRequest request,
                                                               Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        ContactDetailsWrapper contactDetailsWrapper = request.finremCaseDetails.getData()
            .getContactDetailsWrapper();

        if (isRepresentingApplicant(request)) {
            contactDetailsWrapper.setApplicantAddress(serviceAddress);
            contactDetailsWrapper.setApplicantAddressHiddenFromRespondent(YesOrNo.forValue(isConfidential));
        } else if (isRepresentingRespondent(request)) {
            contactDetailsWrapper.setRespondentAddress(serviceAddress);
            contactDetailsWrapper.setRespondentAddressHiddenFromApplicant(YesOrNo.forValue(isConfidential));
        }
    }

    private void populateServiceAddressToIntervener(StopRepresentingRequest request,
                                                    Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        if (isRepresentingAnyIntervenerBarristers(request)) {
            // could be null and skip this population
            return;
        } else {
            if (serviceAddress == null) {
                throw new IllegalStateException("serviceAddress is null");
            }
        }
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        IntervenerWrapper intervenerWrapper = getIntervenerWrapperFromFinremCaseData(request);
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
        return isRepresentingApplicant(request) ? APPLICANT
            : (isRepresentingRespondent(request) ? RESPONDENT : null);
    }

    private IntervenerWrapper getIntervenerWrapperFromFinremCaseData(StopRepresentingRequest request) {
        List<IntervenerWrapper> intervenerWrappers = request.finremCaseDetails.getData().getInterveners();
        return of(getIntervenerIndex(request))
            .map(i -> i - 1) // starts with 1
            .map(intervenerWrappers::get).orElseThrow(IllegalStateException::new);
    }

    private Barrister getIntervenerBarristerFromFinremCaseData(StopRepresentingRequest request) {
        return emptyIfNull(
            request.finremCaseDetails.getData().getBarristerCollectionWrapper()
                .getIntervenerBarristersByIndex(getIntervenerIndex(request))
        ).stream()
            .map(BarristerCollectionItem::getValue)
            .filter(d -> getUserId(request).equals(d.getUserId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(format("%s - unable to locate current intervener barrister profile",
                request.caseId)));
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
        if (isRepresentingRespondent(request)) {
            return "respondent";
        } else if (isRepresentingApplicant(request)) {
            return "applicant";
        } else if (isRepresentingAnyInterveners(request)) {
            return format("intervener %s", getIntervenerIndex(request));
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

    private boolean doesMatchOrganisation(Organisation org1, IntervenerWrapper intervener) {
        return stopRepresentingClientService.isSameOrganisation(org1,
            ofNullable(intervener.getIntervenerOrganisation()).map(OrganisationPolicy::getOrganisation).orElse(null));
    }

    private boolean doesMatchOrganisation(Organisation org1, Barrister barrister) {
        return stopRepresentingClientService.isSameOrganisation(org1,
            ofNullable(barrister).map(Barrister::getOrganisation).orElse(null));
    }

    private boolean doesMatchOrganisation(Organisation org1, OrganisationPolicy orgPolicy) {
        return stopRepresentingClientService.isSameOrganisation(org1,
            ofNullable(orgPolicy).map(OrganisationPolicy::getOrganisation).orElse(null));
    }

    private void setApplicantUnrepresentedIfOrgMatch(FinremCaseData finremCaseData, Organisation targetOrg) {
        if (doesMatchOrganisation(targetOrg, finremCaseData.getApplicantOrganisationPolicy())) {
            setApplicantUnrepresented(finremCaseData);
        }
    }

    private void setRespondentUnrepresentedIfOrgMatch(FinremCaseData finremCaseData, Organisation targetOrg) {
        if (doesMatchOrganisation(targetOrg, finremCaseData.getRespondentOrganisationPolicy())) {
            setRespondentUnrepresented(finremCaseData);
        }
    }

    private void setIntervenerUnrepresentedIfOrgMatch(FinremCaseData finremCaseData, Organisation targetOrg) {
        setIntervenerUnrepresentedIfOrgMatch(finremCaseData, targetOrg, null);
    }

    private void setIntervenerUnrepresentedIfOrgMatch(FinremCaseData finremCaseData,
                                                      Organisation targetOrg,
                                                      IntervenerWrapper excludingIntervener) {
        finremCaseData.getInterveners().stream()
            .filter(intervener -> excludingIntervener == null || !excludingIntervener.equals(intervener))
            .filter(intervener -> doesMatchOrganisation(targetOrg, intervener))
            .forEach(this::setIntervenerUnrepresented);
    }

}
