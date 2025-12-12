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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private final UpdateRepresentationWorkflowService nocWorkflowService;

    private final CaseRoleService caseRoleService;

    private final ManageBarristerService manageBarristerService;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    record StopRepresentingRequest(FinremCaseData finremCaseData, boolean requestedByApplicantRep) {}

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
            caseRoleService.isApplicantRepresentative(finremCaseData,  userAuthorisation));

        populateWarnings(finremCaseData, warnings);
        logStopRepresentingRequest(stopRepresentingRequest);
        clearOrganisationPolicyAndRelatedBarristerSettings(stopRepresentingRequest);
        populateServiceAddressToApplicantOrRespondent(stopRepresentingRequest, getServiceAddressConfig(finremCaseData));
        processRepresentationChange(finremCaseDetails, finremCaseDataBefore, userAuthorisation);
        
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .warnings(warnings)
            .build();
    }

    private void processRepresentationChange(FinremCaseDetails finremCaseDetails,
                                             FinremCaseData finremCaseDataBefore,
                                             String userAuthorisation) {
        nocWorkflowService.prepareChangeOrganisationRequestAndOrganisationPolicy(finremCaseDetails.getData(),
            finremCaseDataBefore, STOP_REPRESENTING_CLIENT, userAuthorisation);

        barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(finremCaseDetails,
            manageBarristerService.getBarristerChange(finremCaseDetails, finremCaseDataBefore, BarristerParty.APPLICANT),
            STOP_REPRESENTING_CLIENT, userAuthorisation);

        barristerChangeCaseAccessUpdater.updateRepresentationUpdateHistoryForCase(finremCaseDetails,
            manageBarristerService.getBarristerChange(finremCaseDetails, finremCaseDataBefore, BarristerParty.RESPONDENT),
            STOP_REPRESENTING_CLIENT, userAuthorisation);
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
            resolveNocParty(representingRequest).getValue(),
            describeApprovalSource(representingRequest.finremCaseData));
    }

    private Pair<Address, Boolean> getServiceAddressConfig(FinremCaseData finremCaseData) {
        StopRepresentationWrapper wrapper = finremCaseData.getStopRepresentationWrapper();
        return Pair.of(wrapper.getClientAddressForService(), YesOrNo.isYes(wrapper.getClientAddressForServiceConfidential()));
    }

    private void clearOrganisationPolicyAndRelatedBarristerSettings(StopRepresentingRequest stopRepresentingRequest) {
        FinremCaseData caseData = stopRepresentingRequest.finremCaseData;
        NoticeOfChangeParty noticeOfChangeParty = resolveNocParty(stopRepresentingRequest);

        // Set the party who is being unrepresented
        caseData.getContactDetailsWrapper().setNocParty(noticeOfChangeParty);

        boolean isApplicantRepresentativeChange = APPLICANT.equals(noticeOfChangeParty);

        // Determine the relevant policies and barrister lists based on the party
        final OrganisationPolicy organisationPolicy;
        final List<BarristerCollectionItem> barristerCollection;

        if (isApplicantRepresentativeChange) {
            organisationPolicy = caseData.getApplicantOrganisationPolicy();
            barristerCollection = caseData.getBarristerCollectionWrapper().getApplicantBarristers();
            caseData.setApplicantOrganisationPolicy(getDefaultOrganisationPolicy(CaseRole.APP_SOLICITOR));
        } else {
            organisationPolicy = caseData.getRespondentOrganisationPolicy();
            barristerCollection = caseData.getBarristerCollectionWrapper().getRespondentBarristers();
            caseData.setRespondentOrganisationPolicy(getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR));
        }

        // Extract the Organisation ID to remove (centralized logic)
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
        NoticeOfChangeParty noticeOfChangeParty = resolveNocParty(stopRepresentingRequest);

        ContactDetailsWrapper contactDetailsWrapper = stopRepresentingRequest.finremCaseData.getContactDetailsWrapper();

        if (APPLICANT.equals(noticeOfChangeParty)) {
            setApplicantUnrepresented(stopRepresentingRequest.finremCaseData);
            contactDetailsWrapper.setApplicantAddress(serviceAddress);
            contactDetailsWrapper.setApplicantAddressHiddenFromRespondent(YesOrNo.forValue(isConfidential));
        } else { // otherwise it is being requested by respondent representatives
            setRespondentUnrepresented(stopRepresentingRequest.finremCaseData);
            contactDetailsWrapper.setRespondentAddress(serviceAddress);
            contactDetailsWrapper.setRespondentAddressHiddenFromApplicant(YesOrNo.forValue(isConfidential));
        }
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

    private NoticeOfChangeParty resolveNocParty(StopRepresentingRequest stopRepresentingRequest) {
        return stopRepresentingRequest.requestedByApplicantRep ? APPLICANT : RESPONDENT;
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
