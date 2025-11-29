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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    record StopRepresentingRequest(FinremCaseData finremCaseData, boolean isLoginWithApplicantSolicitor) {}

    private static final String WARNING_MESSAGE =
        "Are you sure you wish to stop representing your client? "
            + "If you continue your access to this access will be removed";

    public StopRepresentingClientAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      UpdateRepresentationWorkflowService nocWorkflowService,
                                                      CaseRoleService caseRoleService) {
        super(finremCaseDetailsMapper);
        this.caseRoleService = caseRoleService;
        this.nocWorkflowService = nocWorkflowService;
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
        final boolean isLoginWithApplicantSolicitor =
            caseRoleService.isLoginWithApplicantSolicitor(finremCaseData, userAuthorisation);
        final List<String> warnings = new ArrayList<>();
        StopRepresentingRequest stopRepresentingRequest = new StopRepresentingRequest(finremCaseData, isLoginWithApplicantSolicitor);

        populateWarnings(finremCaseData, warnings);
        logStopRepresentingRequest(stopRepresentingRequest);
        setPartyToChangeRepresented(stopRepresentingRequest);
        setServiceAddress(stopRepresentingRequest, getServiceAddressConfig(finremCaseData));
        processRepresentationChange(finremCaseData, finremCaseDetails.getData(), userAuthorisation);
        
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .warnings(warnings)
            .build();
    }

    private void processRepresentationChange(FinremCaseData finremCaseData,
                                             FinremCaseData finremCaseDataBefore,
                                             String userAuthorisation) {
        nocWorkflowService.handleNoticeOfChangeWorkflow(finremCaseData, finremCaseDataBefore,
            userAuthorisation);
    }

    private void populateWarnings(FinremCaseData finremCaseData, List<String> warnings) {
        if (isHavingClientConsent(finremCaseData) || isHavingJudicialApproval(finremCaseData)) {
            warnings.add(WARNING_MESSAGE);
        } else {
            throw new IllegalStateException("Client consent or judicial approval is required but missing.");
        }
    }

    private void logStopRepresentingRequest(StopRepresentingRequest representingRequest) {
        log.info("{} - {} solicitor stops representing a client with a {}", representingRequest.finremCaseData.getCcdCaseId(),
            resolveNocParty(representingRequest.isLoginWithApplicantSolicitor).getValue(),
            describeApprovalSource(representingRequest.finremCaseData));
    }

    private Pair<Address, Boolean> getServiceAddressConfig(FinremCaseData finremCaseData) {
        StopRepresentationWrapper wrapper = finremCaseData.getStopRepresentationWrapper();
        return Pair.of(wrapper.getClientAddressForService(), YesOrNo.isYes(wrapper.getClientAddressForServiceConfidential()));
    }

    private void setPartyToChangeRepresented(StopRepresentingRequest stopRepresentingRequest) {
        stopRepresentingRequest.finremCaseData.getContactDetailsWrapper()
            .setNocParty(resolveNocParty(stopRepresentingRequest.isLoginWithApplicantSolicitor));
    }

    private void setServiceAddress(StopRepresentingRequest stopRepresentingRequest,
                                   Pair<Address, Boolean> serviceAddressConfig) {
        Address serviceAddress = serviceAddressConfig.getLeft();
        boolean isConfidential = Boolean.TRUE.equals(serviceAddressConfig.getRight());

        ContactDetailsWrapper contactDetailsWrapper = stopRepresentingRequest.finremCaseData.getContactDetailsWrapper();

        if (stopRepresentingRequest.isLoginWithApplicantSolicitor) {
            setApplicantUnrepresented(stopRepresentingRequest.finremCaseData);
            contactDetailsWrapper.setApplicantAddress(serviceAddress);
            contactDetailsWrapper.setApplicantAddressHiddenFromRespondent(YesOrNo.forValue(isConfidential));
        } else {
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

    private NoticeOfChangeParty resolveNocParty(boolean isLoginWithApplicantSolicitor) {
        return isLoginWithApplicantSolicitor ? APPLICANT : RESPONDENT;
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
}
