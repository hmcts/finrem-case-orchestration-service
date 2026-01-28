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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.RepresentativeInContext;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation.isSameOrganisation;

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
                                   RepresentativeInContext representativeInContext) {}

    private static final String WARNING_MESSAGE =
        "Are you sure you wish to stop representing your client? "
            + "If you continue your access to this access will be removed";

    private static boolean isRepresentingApplicant(StopRepresentingRequest request) {
        return request.representativeInContext.isApplicationRepresentative();
    }

    private static boolean isRepresentingRespondent(StopRepresentingRequest request) {
        return request.representativeInContext.isRespondentRepresentative();
    }

    private static boolean isRepresentingAnyInterveners(StopRepresentingRequest request) {
        return request.representativeInContext.isIntervenerRepresentative();
    }

    private static boolean isRepresentingAnyIntervenerBarristers(StopRepresentingRequest request) {
        return request.representativeInContext.isIntervenerBarrister();
    }

    private static String getUserId(StopRepresentingRequest request) {
        return request.representativeInContext.userId();
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
        return of(request.representativeInContext.intervenerIndex()).orElseThrow(()
            -> new IllegalStateException(format("%s - expecting intervener index exists", request.caseId)));
    }

    private boolean hasApplicantOrRespondentOrganisationPolicyChange(StopRepresentingRequest request) {
        FinremCaseData caseData = request.finremCaseDetails.getData();
        return caseData.getContactDetailsWrapper().getNocParty() != null;
    }

    private void buildRepresentationUpdateHistoryAndNocDataIfAny(StopRepresentingRequest request, String userAuthorisation) {
        FinremCaseData finremCaseData = request.finremCaseDetails.getData();
        FinremCaseData originalFinremCaseData = request.finremCaseDetailsBefore.getData();

        if (isRepresentingApplicant(request)
            || isRepresentingRespondent(request)
            || (isRepresentingAnyInterveners(request) && hasApplicantOrRespondentOrganisationPolicyChange(request))
        ) {
            // below also update the representative history
            nocWorkflowService.prepareNoticeOfChangeAndOrganisationPolicy(finremCaseData,
                originalFinremCaseData, STOP_REPRESENTING_CLIENT, userAuthorisation);
        }

        // update representation update history for intervener solicitors' change (1-4)
        intervenerService.updateIntervenerSolicitorStopRepresentingHistory(finremCaseData,
            originalFinremCaseData, userAuthorisation);

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

    private record ExtraClientAddress(
        String id,
        Address address,
        YesOrNo confidential
    ) {}

    private Pair<Address, Boolean> getServiceAddressConfigIfAny(FinremCaseData finremCaseData, ExtraAddrType expectedType) {
        StopRepresentationWrapper wrapper = finremCaseData.getStopRepresentationWrapper();

        List<ExtraClientAddress> extraAddresses = List.of(
            new ExtraClientAddress(wrapper.getExtraClientAddr1Id(), wrapper.getExtraClientAddr1(),
                wrapper.getExtraClientAddr1Confidential()),
            new ExtraClientAddress(wrapper.getExtraClientAddr2Id(), wrapper.getExtraClientAddr2(),
                wrapper.getExtraClientAddr2Confidential()),
            new ExtraClientAddress(wrapper.getExtraClientAddr3Id(), wrapper.getExtraClientAddr3(),
                wrapper.getExtraClientAddr3Confidential()),
            new ExtraClientAddress(wrapper.getExtraClientAddr4Id(), wrapper.getExtraClientAddr4(),
                wrapper.getExtraClientAddr4Confidential())
        );

        return extraAddresses.stream()
            .filter(a -> expectedType.getId().equals(a.id()))
            .findFirst()
            .map(a -> Pair.of(
                a.address(),
                YesOrNo.isYes(a.confidential())
            ))
            .orElse(null);
    }

    private Pair<Address, Boolean> getServiceAddressConfigForApplicantIfAny(FinremCaseData finremCaseData) {
        return getServiceAddressConfigIfAny(finremCaseData, ExtraAddrType.APPLICANT);
    }

    private Pair<Address, Boolean> getServiceAddressConfigForRespondentIfAny(FinremCaseData finremCaseData) {
        return getServiceAddressConfigIfAny(finremCaseData, ExtraAddrType.RESPONDENT);
    }

    private Pair<Address, Boolean> getServiceAddressConfigForIntervener1IfAny(FinremCaseData finremCaseData) {
        return getServiceAddressConfigIfAny(finremCaseData, ExtraAddrType.INTERVENER1);
    }

    private Pair<Address, Boolean> getServiceAddressConfigForIntervener2IfAny(FinremCaseData finremCaseData) {
        return getServiceAddressConfigIfAny(finremCaseData, ExtraAddrType.INTERVENER2);
    }

    private Pair<Address, Boolean> getServiceAddressConfigForIntervener3IfAny(FinremCaseData finremCaseData) {
        return getServiceAddressConfigIfAny(finremCaseData, ExtraAddrType.INTERVENER3);
    }

    private Pair<Address, Boolean> getServiceAddressConfigForIntervener4IfAny(FinremCaseData finremCaseData) {
        return getServiceAddressConfigIfAny(finremCaseData, ExtraAddrType.INTERVENER4);
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

        // reset noc party
        finremCaseData.getContactDetailsWrapper().setNocParty(null);

        if (isRepresentingApplicant(request)) {
            stopRepresentingByApplicantRepresentative(finremCaseData);
        } else if (isRepresentingRespondent(request)) {
            stopRepresentingByRespondentRepresentative(finremCaseData);
        } else if (isRepresentingAnyInterveners(request)) {
            if (isRepresentingAnyIntervenerBarristers(request)) {
                stopRepresentingByIntervenerBarrister(finremCaseData, getIntervenerBarrister(request));
            } else {
                // it must be intervener solicitor
                stopRepresentingByIntervenerSolicitor(finremCaseData, getIntervenerWrapper(request));
            }
        }
    }

    private void populateServiceAddressToParty(StopRepresentingRequest request) {
        FinremCaseData finremCaseData = request.finremCaseDetails.getData();
        Pair<Address, Boolean> serviceAddressConfig = getServiceAddressConfig(finremCaseData);
        if (isRepresentingApplicant(request) || isRepresentingRespondent(request)) {
            populateMainServiceAddressToApplicantOrRespondent(request, serviceAddressConfig);
        } else if (isRepresentingAnyInterveners(request)) {
            populateMainServiceAddressToIntervener(request, serviceAddressConfig);
        }

        // extra service address to be captured
        populateServiceAddressToApplicant(finremCaseData, getServiceAddressConfigForApplicantIfAny(finremCaseData));
        populateServiceAddressToRespondent(finremCaseData, getServiceAddressConfigForRespondentIfAny(finremCaseData));
        populateServiceAddressToIntervener(finremCaseData.getIntervenerOne(), getServiceAddressConfigForIntervener1IfAny(finremCaseData));
        populateServiceAddressToIntervener(finremCaseData.getIntervenerTwo(), getServiceAddressConfigForIntervener2IfAny(finremCaseData));
        populateServiceAddressToIntervener(finremCaseData.getIntervenerThree(), getServiceAddressConfigForIntervener3IfAny(finremCaseData));
        populateServiceAddressToIntervener(finremCaseData.getIntervenerFour(), getServiceAddressConfigForIntervener4IfAny(finremCaseData));
    }

    private void populateServiceAddressToApplicant(FinremCaseData finremCaseData, Pair<Address, Boolean> serviceAddressConfig) {
        if (serviceAddressConfig == null) {
            return;
        }
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        ContactDetailsWrapper contactDetailsWrapper = finremCaseData.getContactDetailsWrapper();
        contactDetailsWrapper.setApplicantAddress(serviceAddress);
        contactDetailsWrapper.setApplicantAddressHiddenFromRespondent(YesOrNo.forValue(isConfidential));
    }

    private void populateServiceAddressToRespondent(FinremCaseData finremCaseData, Pair<Address, Boolean> serviceAddressConfig) {
        if (serviceAddressConfig == null) {
            return;
        }
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        ContactDetailsWrapper contactDetailsWrapper = finremCaseData.getContactDetailsWrapper();
        contactDetailsWrapper.setRespondentAddress(serviceAddress);
        contactDetailsWrapper.setRespondentAddressHiddenFromApplicant(YesOrNo.forValue(isConfidential));
    }

    private void populateServiceAddressToIntervener(IntervenerWrapper intervenerWrapper, Pair<Address, Boolean> serviceAddressConfig) {
        if (serviceAddressConfig == null) {
            return;
        }
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        intervenerWrapper.setIntervenerAddress(serviceAddress);
        intervenerWrapper.setIntervenerAddressConfidential(YesOrNo.forValue(isConfidential));
    }

    private void throwIfServiceAddressIsNull(Address serviceAddress) {
        if (serviceAddress == null) {
            throw new IllegalStateException("serviceAddress is null");
        }
    }

    private void populateMainServiceAddressToApplicantOrRespondent(StopRepresentingRequest request,
                                                                   Pair<Address, Boolean> serviceAddressConfig) {
        FinremCaseData finremCaseData = request.finremCaseDetails.getData();
        Address serviceAddress = serviceAddressConfig.getLeft();
        throwIfServiceAddressIsNull(serviceAddress);

        if (isRepresentingApplicant(request)) {
            populateServiceAddressToApplicant(finremCaseData, serviceAddressConfig);
        } else if (isRepresentingRespondent(request)) {
            populateServiceAddressToRespondent(finremCaseData, serviceAddressConfig);
        }
    }

    private void populateMainServiceAddressToIntervener(StopRepresentingRequest request,
                                                        Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        if (isRepresentingAnyIntervenerBarristers(request) && serviceAddress == null) {
            // serviceAddress could be null and skip this population
            return;
        } else {
            throwIfServiceAddressIsNull(serviceAddress);
        }
        IntervenerWrapper intervenerWrapper = getIntervenerWrapper(request);
        populateServiceAddressToIntervener(intervenerWrapper, serviceAddressConfig);
    }

    private void setApplicantUnrepresented(FinremCaseData finremCaseData) {
        stopRepresentingClientService.setApplicantUnrepresented(finremCaseData);
        finremCaseData.getContactDetailsWrapper().setNocParty(APPLICANT);
    }

    private void setRespondentUnrepresented(FinremCaseData finremCaseData) {
        stopRepresentingClientService.setRespondentUnrepresented(finremCaseData);
        finremCaseData.getContactDetailsWrapper().setNocParty(RESPONDENT);
    }

    private void setIntervenerUnrepresented(IntervenerWrapper intervenerWrapper) {
        stopRepresentingClientService.setIntervenerUnrepresented(intervenerWrapper);
    }

    private IntervenerWrapper getIntervenerWrapper(StopRepresentingRequest request) {
        return request.finremCaseDetails.getData().getIntervenerById(getIntervenerIndex(request));
    }

    private Barrister getIntervenerBarrister(StopRepresentingRequest request) {
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

    private boolean doesMatchOrganisation(Organisation org1, IntervenerWrapper intervener) {
        return isSameOrganisation(org1,
            ofNullable(intervener.getIntervenerOrganisation()).map(OrganisationPolicy::getOrganisation).orElse(null));
    }

    private boolean doesMatchOrganisation(Organisation org1, Barrister barrister) {
        return isSameOrganisation(org1,
            ofNullable(barrister).map(Barrister::getOrganisation).orElse(null));
    }

    private boolean doesMatchOrganisation(Organisation org1, OrganisationPolicy orgPolicy) {
        return isSameOrganisation(org1,
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
