package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UpdateContactDetailsAboutToSubmitHandler extends FinremCallbackHandler {

    private final UpdateContactDetailsService updateContactDetailsService;
    private final OnlineFormDocumentService onlineFormDocumentService;
    private final UpdateRepresentationWorkflowService nocWorkflowService;

    public UpdateContactDetailsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    UpdateContactDetailsService updateContactDetailsService,
                                                    OnlineFormDocumentService onlineFormDocumentService,
                                                    UpdateRepresentationWorkflowService nocWorkflowService
    ) {
        super(finremCaseDetailsMapper);
        this.updateContactDetailsService = updateContactDetailsService;
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.nocWorkflowService = nocWorkflowService;
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
        log.info("Invoking contested {} about to submit callback for Case ID: {}",
            callbackRequest.getEventType(), finremCaseDetails.getId());
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        Optional<ContactDetailsWrapper> contactDetailsWrapper = Optional.ofNullable(finremCaseData.getContactDetailsWrapper());

        boolean includeRepresentationChange = contactDetailsWrapper
            .map(wrapper -> wrapper.getUpdateIncludesRepresentativeChange() == YesOrNo.YES)
            .orElse(false);

        YesOrNo isRespondentAddressHidden = contactDetailsWrapper
            .map(ContactDetailsWrapper::getRespondentAddressHiddenFromApplicant)
            .orElse(YesOrNo.NO);

        YesOrNo isApplicantAddressHidden = contactDetailsWrapper
            .map(ContactDetailsWrapper::getApplicantAddressHiddenFromRespondent)
            .orElse(YesOrNo.NO);

        if (includeRepresentationChange) {
            updateContactDetailsService.handleRepresentationChange(finremCaseData, finremCaseDetails.getCaseType());
        }

        if (isRespondentAddressHidden == YesOrNo.YES || isApplicantAddressHidden == YesOrNo.YES) {
            CaseDocument document = onlineFormDocumentService.generateContestedMiniForm(userAuthorisation, finremCaseDetails);
            finremCaseData.setMiniFormA(document);
        }

        if (includeRepresentationChange) {
            CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
            CaseDetails caseDetailsBefore = finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetailsBefore());

            Map<String, Object> updateCaseData = nocWorkflowService
                .handleNoticeOfChangeWorkflow(caseDetails, userAuthorisation, caseDetailsBefore)
                .getData();

            finremCaseData = finremCaseDetailsMapper.mapToFinremCaseData(updateCaseData, caseDetails.getCaseTypeId());

        } else {
            updateContactDetailsService.persistOrgPolicies(finremCaseData, callbackRequest.getCaseDetailsBefore().getData());
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }
}
