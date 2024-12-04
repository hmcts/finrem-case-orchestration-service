package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

@Slf4j
@Service
public class UpdateContactDetailsAboutToSubmitHandler extends FinremCallbackHandler {

    private final UpdateContactDetailsService updateContactDetailsService;
    private final OnlineFormDocumentService onlineFormDocumentService;

    public UpdateContactDetailsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, UpdateContactDetailsService updateContactDetailsService, OnlineFormDocumentService onlineFormDocumentService) {
        super(finremCaseDetailsMapper);
        this.updateContactDetailsService = updateContactDetailsService;
        this.onlineFormDocumentService = onlineFormDocumentService;
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

        ContactDetailsWrapper contactDetailsWrapper = finremCaseData.getContactDetailsWrapper();
        boolean includeRepresentationChange = contactDetailsWrapper.getUpdateIncludesRepresentativeChange().isYes();

        if (includeRepresentationChange) {
            updateContactDetailsService.handleRepresentationChange(finremCaseDetails);
        }

        YesOrNo isRespondentAddressHidden  = contactDetailsWrapper.getRespondentAddressHiddenFromApplicant();
        YesOrNo isApplicantAddressHidden = contactDetailsWrapper.getApplicantAddressHiddenFromRespondent();

        if ((isRespondentAddressHidden != null && isRespondentAddressHidden.isYes())
            || (isApplicantAddressHidden != null && isApplicantAddressHidden.isYes())) {
            CaseDocument document = onlineFormDocumentService.generateContestedMiniForm(userAuthorisation, finremCaseDetails);
            finremCaseData.setMiniFormA(document);
        }

        if (!includeRepresentationChange) {
            updateContactDetailsService.persistOrgPolicies(finremCaseData, callbackRequest.getCaseDetailsBefore().getData());
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }
}
