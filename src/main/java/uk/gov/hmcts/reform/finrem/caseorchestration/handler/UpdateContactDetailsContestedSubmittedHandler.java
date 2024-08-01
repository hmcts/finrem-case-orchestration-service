package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;

@Slf4j
@Service
public class UpdateContactDetailsContestedSubmittedHandler extends FinremCallbackHandler {

    private final UpdateRepresentationWorkflowService nocWorkflowService;

    private final OnlineFormDocumentService service;


    public UpdateContactDetailsContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         UpdateRepresentationWorkflowService nocWorkflowService,
                                                         OnlineFormDocumentService service) {
        super(finremCaseDetailsMapper);
        this.nocWorkflowService = nocWorkflowService;
        this.service = service;
    }


    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
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

        if(caseData.isAppAddressConfidential() || caseData.isRespAddressConfidential()) {
            CaseDocument document = service.generateContestedMiniForm(userAuthorisation, finremCaseDetails);
            caseData.setMiniFormA(document);
        }

        if(Optional.ofNullable(caseData.getContactDetailsWrapper().getUpdateIncludesRepresentativeChange()).isPresent()
        && caseData.getContactDetailsWrapper().equals(YES_VALUE)) {
            FinremCaseDetails originalfinremCaseDetails = callbackRequest.getCaseDetailsBefore();
            CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
            CaseDetails originalCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(originalfinremCaseDetails);

            return ResponseEntity.ok(nocWorkflowService.handleNoticeOfChangeWorkflow(caseDetails,
                userAuthorisation,
                originalCaseDetails));


            return ResponseEntity<GenericAboutToStartOrSubmitCallbackResponse>
        }

        persistOrgPolicies(caseData, callbackRequest.getCaseDetailsBefore());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseDetails.getData()).build();
    }


    private void removeApplicantSolicitorDetails(FinremCaseData caseData) {

        if(!caseData.isApplicantRepresentedByASolicitor()) {
            caseData.getContactDetailsWrapper().setApplicantSolicitorName(null);
            caseData.getContactDetailsWrapper().setApplicantSolicitorFirm(null);
            caseData.getContactDetailsWrapper().setApplicantSolicitorAddress(null);
            caseData.getContactDetailsWrapper().setApplicantSolicitorPhone(null);
            caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(null);
            caseData.getContactDetailsWrapper().setApplicantSolicitorDxNumber(null);
            caseData.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(null);
            // removes Applicant Organisation Policy of Solicitor
            caseData.setApplicantOrganisationPolicy(null);
            persistOrgPolicies(caseData);
        }
    }

    private void removeRespondentDetails(FinremCaseData caseData) {

        if(caseData.isRespondentRepresentedByASolicitor()) {
            caseData.getContactDetailsWrapper().setRespondentAddress(null);
            caseData.getContactDetailsWrapper().setRespondentPhone(null);
            caseData.getContactDetailsWrapper().setRespondentEmail(null);
            caseData.getContactDetailsWrapper().setRespondentResideOutsideUK(null);
        } else {
            caseData.getContactDetailsWrapper().setRespondentSolicitorName(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorFirm(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorPhone(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorDxNumber(null);
            caseData.setRespSolNotificationsEmailConsent(null);
            caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(null);
            // removes Respondent Organisation Policy of Solicitor
            caseData.setRespondentOrganisationPolicy(null);
            persistOrgPolicies(caseData);
        }
    }

    private void persistOrgPolicies(FinremCaseData caseData) {
        nocWorkflowService.persistDefaultOrganisationPolicy(caseData);
    }
}