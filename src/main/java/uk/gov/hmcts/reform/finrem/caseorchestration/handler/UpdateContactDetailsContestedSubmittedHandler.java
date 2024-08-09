package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Optional;

@Slf4j
@Service
public class UpdateContactDetailsContestedSubmittedHandler extends FinremCallbackHandler {

    private final UpdateRepresentationWorkflowService nocWorkflowService;
    private final OnlineFormDocumentService onlineFormDocumentService;

    public UpdateContactDetailsContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         UpdateRepresentationWorkflowService nocWorkflowService,
                                                         OnlineFormDocumentService service) {
        super(finremCaseDetailsMapper);
        this.nocWorkflowService = nocWorkflowService;
        this.onlineFormDocumentService = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} submitted callback for Case ID: {}", callbackRequest.getEventType(),
            finremCaseDetails.getId());

        FinremCaseData caseData = finremCaseDetails.getData();

        removeApplicantSolicitorDetails(caseData);
        removeRespondentDetails(caseData);

        if (caseData.isAppAddressConfidential() || caseData.isRespAddressConfidential()) {
            CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails());
            CaseDocument document = onlineFormDocumentService.generateContestedMiniFormA(userAuthorisation, caseDetails);
            caseData.setMiniFormA(document);
        }

        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        if (isRepresentativeChange(caseDataBefore, caseData)) {
            updateNocParty(caseDataBefore, caseData);
            return handleNoticeOfChangeWorkflow(callbackRequest, userAuthorisation);
        } else {
            persistOrgPolicies(caseData, caseDataBefore);

            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(finremCaseDetails.getData()).build();
        }
    }

    private boolean isRepresentativeChange(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        return isUpdateIncludesRepresentativeChange(caseData.getContactDetailsWrapper())
            || isApplicantRepresentedChange(caseDataBefore, caseData)
            || isRespondentRepresentedChange(caseDataBefore, caseData);
    }

    private boolean isUpdateIncludesRepresentativeChange(ContactDetailsWrapper contactDetailsWrapper) {
        return Optional.ofNullable(contactDetailsWrapper.getUpdateIncludesRepresentativeChange())
            .orElse(YesOrNo.NO)
            .isYes();
    }

    private boolean isApplicantRepresentedChange(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        return caseDataBefore.isApplicantRepresentedByASolicitor() != caseData.isApplicantRepresentedByASolicitor();
    }

    private boolean isRespondentRepresentedChange(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        return caseDataBefore.isRespondentRepresentedByASolicitor() != caseData.isRespondentRepresentedByASolicitor();
    }

    private void updateNocParty(FinremCaseData caseDataBefore, FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        if (isUpdateIncludesRepresentativeChange(contactDetailsWrapper)) {
            return;
        }
        if (isApplicantRepresentedChange(caseDataBefore, caseData)) {
            contactDetailsWrapper.setUpdateIncludesRepresentativeChange(YesOrNo.YES);
            contactDetailsWrapper.setNocParty(NoticeOfChangeParty.APPLICANT);
        }
        if (isRespondentRepresentedChange(caseDataBefore, caseData)) {
            contactDetailsWrapper.setUpdateIncludesRepresentativeChange(YesOrNo.YES);
            contactDetailsWrapper.setNocParty(NoticeOfChangeParty.RESPONDENT);
        }
    }

    private GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleNoticeOfChangeWorkflow(
        FinremCallbackRequest request, String userAuthorisation) {
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(request.getCaseDetails());
        CaseDetails originalCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(request.getCaseDetailsBefore());
        AboutToStartOrSubmitCallbackResponse nocResponse = nocWorkflowService.handleNoticeOfChangeWorkflow(
            caseDetails, userAuthorisation, originalCaseDetails);

        CaseType caseType = request.getCaseDetails().getCaseType();
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseDetailsMapper.mapToFinremCaseData(nocResponse.getData(), caseType.getCcdType()))
            .dataClassification(nocResponse.getDataClassification())
            .securityClassification(nocResponse.getSecurityClassification())
            .errors(nocResponse.getErrors())
            .warnings(nocResponse.getWarnings())
            .state(nocResponse.getState())
            .build();
    }

    private void removeApplicantSolicitorDetails(FinremCaseData caseData) {
        if (!caseData.isApplicantRepresentedByASolicitor()) {
            ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
            contactDetailsWrapper.setApplicantSolicitorName(null);
            contactDetailsWrapper.setApplicantSolicitorFirm(null);
            contactDetailsWrapper.setApplicantSolicitorAddress(null);
            contactDetailsWrapper.setApplicantSolicitorPhone(null);
            contactDetailsWrapper.setApplicantSolicitorEmail(null);
            contactDetailsWrapper.setApplicantSolicitorDxNumber(null);
            contactDetailsWrapper.setApplicantSolicitorConsentForEmails(null);
            caseData.setApplicantOrganisationPolicy(null);
        }
    }

    private void removeRespondentDetails(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        if (caseData.isRespondentRepresentedByASolicitor()) {
            contactDetailsWrapper.setRespondentAddress(null);
            contactDetailsWrapper.setRespondentPhone(null);
            contactDetailsWrapper.setRespondentEmail(null);
            contactDetailsWrapper.setRespondentResideOutsideUK(null);
        } else {
            contactDetailsWrapper.setRespondentSolicitorName(null);
            contactDetailsWrapper.setRespondentSolicitorFirm(null);
            contactDetailsWrapper.setRespondentSolicitorAddress(null);
            contactDetailsWrapper.setRespondentSolicitorPhone(null);
            contactDetailsWrapper.setRespondentSolicitorEmail(null);
            contactDetailsWrapper.setRespondentSolicitorDxNumber(null);
            caseData.setRespSolNotificationsEmailConsent(null);
            caseData.setRespondentOrganisationPolicy(null);
        }
    }

    private void persistOrgPolicies(FinremCaseData caseData, FinremCaseData originalDetails) {
        caseData.setApplicantOrganisationPolicy(originalDetails.getApplicantOrganisationPolicy());
        caseData.setRespondentOrganisationPolicy(originalDetails.getRespondentOrganisationPolicy());
    }
}
