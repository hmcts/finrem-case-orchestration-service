package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsService;

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

    private final UpdateContactDetailsService updateContactDetailsService;

    private final CaseRoleService caseRoleService;

    private static final String WARNING_MESSAGE =
        "Are you sure you wish to stop representing your client? "
            + "If you continue your access to this access will be removed";

    public StopRepresentingClientAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      UpdateContactDetailsService updateContactDetailsService,
                                                      CaseRoleService caseRoleService) {
        super(finremCaseDetailsMapper);
        this.updateContactDetailsService = updateContactDetailsService;
        this.caseRoleService = caseRoleService;
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

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        List<String> warnings = new ArrayList<>();
        if (isHavingClientConsent(finremCaseData) || isHavingJudicialApproval(finremCaseData)) {
            warnings.add(WARNING_MESSAGE);
        } else {
            throw new IllegalStateException("Client consent or judicial approval is required but missing.");
        }

        final boolean isLoginWithApplicantSolicitor =
            caseRoleService.isLoginWithApplicantSolicitor(finremCaseData, userAuthorisation);
        log.info("{} - {} solicitor stops representing a client with a {}", finremCaseData.getCcdCaseId(),
            resolveNocParty(isLoginWithApplicantSolicitor).getValue(), describeApprovalSource(finremCaseData));

        Address clientAddressForService = finremCaseData.getStopRepresentationWrapper().getClientAddressForService();
        boolean isAddressConfidential = YesOrNo.isYes(finremCaseData.getStopRepresentationWrapper().getClientAddressForServiceConfidential());

        finremCaseData.getContactDetailsWrapper().setNocParty(resolveNocParty(isLoginWithApplicantSolicitor));
        if (isLoginWithApplicantSolicitor) {
            finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
            finremCaseData.getContactDetailsWrapper().setApplicantAddress(clientAddressForService);
            finremCaseData.getContactDetailsWrapper().setApplicantAddressHiddenFromRespondent(YesOrNo.forValue(isAddressConfidential));
        } else {
            setRespondentUnrepresented(finremCaseDetails);
            finremCaseData.getContactDetailsWrapper().setRespondentAddress(clientAddressForService);
            finremCaseData.getContactDetailsWrapper().setRespondentAddressHiddenFromApplicant(YesOrNo.forValue(isAddressConfidential));
        }
        updateContactDetailsService.handleRepresentationChange(finremCaseData, finremCaseDetails.getCaseType());
        //        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        //        CaseDetails caseDetailsBefore = finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetailsBefore());
        //
        //        Map<String, Object> updateCaseData = TODO nocWorkflowService
        //            .handleNoticeOfChangeWorkflow(caseDetails, userAuthorisation, caseDetailsBefore)
        //            .getData();
        //
        //        finremCaseData = finremCaseDetailsMapper.mapToFinremCaseData(updateCaseData, caseDetails.getCaseTypeId());
        
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .warnings(warnings)
            .build();
    }

    private void setRespondentUnrepresented(FinremCaseDetails finremCaseDetails) {
        if (finremCaseDetails.isConsentedApplication()) {
            finremCaseDetails.getData().getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);
        } else {
            finremCaseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
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
