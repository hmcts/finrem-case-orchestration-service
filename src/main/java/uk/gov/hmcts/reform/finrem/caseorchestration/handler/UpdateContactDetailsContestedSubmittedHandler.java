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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;

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
        log.info("Invoking contested {} submitted callback for Case ID: {}", callbackRequest.getEventType(), finremCaseDetails.getId());

        FinremCaseData caseData = finremCaseDetails.getData();

        removeApplicantSolicitorDetails(caseData);
        removeRespondentDetails(caseData);

        if (caseData.isAppAddressConfidential() || caseData.isRespAddressConfidential()) {
            CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails());
            CaseDocument document = onlineFormDocumentService.generateContestedMiniFormA(userAuthorisation, caseDetails);
            caseData.setMiniFormA(document);
        }

        if (Optional.ofNullable(caseData.getContactDetailsWrapper().getUpdateIncludesRepresentativeChange()).isPresent()
            && caseData.getContactDetailsWrapper().getUpdateIncludesRepresentativeChange().toString().equals(YES_VALUE)) {
            return handleNoticeOfChangeWorklow(callbackRequest, userAuthorisation);
        }


        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        boolean isApplicantRepresentedChanged = caseDataBefore.isApplicantRepresentedByASolicitor()
            != caseData.isApplicantRepresentedByASolicitor();
        boolean isRespondentRepresentedChanged = caseDataBefore.isRespondentRepresentedByASolicitor()
            != caseData.isRespondentRepresentedByASolicitor();

        if (isApplicantRepresentedChanged || isRespondentRepresentedChanged) {
            return handleNoticeOfChangeWorklow(callbackRequest, userAuthorisation);
        }

        persistOrgPolicies(caseData, callbackRequest.getCaseDetailsBefore().getData());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseDetails.getData()).build();
    }

    private GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handleNoticeOfChangeWorklow(
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

        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        if (!caseData.isApplicantRepresentedByASolicitor()) {
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
            caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(null);
            caseData.setRespondentOrganisationPolicy(null);
        }
    }

    private void persistOrgPolicies(FinremCaseData caseData, FinremCaseData originalDetails) {
        caseData.setApplicantOrganisationPolicy(originalDetails.getApplicantOrganisationPolicy());
        caseData.setRespondentOrganisationPolicy(originalDetails.getRespondentOrganisationPolicy());
    }
}
