package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UpdateContactDetailsAboutToSubmitHandler extends FinremCallbackHandler {

    private final UpdateContactDetailsService updateContactDetailsService;
    private final OnlineFormDocumentService onlineFormDocumentService;
    private final UpdateRepresentationWorkflowService nocWorkflowService;
    private final InternationalPostalService internationalPostalService;

    public UpdateContactDetailsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    UpdateContactDetailsService updateContactDetailsService,
                                                    OnlineFormDocumentService onlineFormDocumentService,
                                                    UpdateRepresentationWorkflowService nocWorkflowService,
                                                    InternationalPostalService internationalPostalService
    ) {
        super(finremCaseDetailsMapper);
        this.updateContactDetailsService = updateContactDetailsService;
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.nocWorkflowService = nocWorkflowService;
        this.internationalPostalService = internationalPostalService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        Set<CaseType> validCaseTypes = Set.of(CaseType.CONTESTED, CaseType.CONSENTED);
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && validCaseTypes.contains(caseType)
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        Optional<ContactDetailsWrapper> contactDetailsWrapper = Optional.ofNullable(finremCaseData.getContactDetailsWrapper());

        boolean includeRepresentationChange = contactDetailsWrapper
            .map(wrapper -> wrapper.getUpdateIncludesRepresentativeChange() == YesOrNo.YES)
            .orElse(false);

        if (includeRepresentationChange) {
            updateContactDetailsService.handleRepresentationChange(finremCaseData, finremCaseDetails.getCaseType());
        }

        if (CaseType.CONTESTED.equals(finremCaseDetails.getCaseType())) {
            considerContestedMiniFormA(finremCaseDetails, userAuthorisation);
        }

        RefugeWrapperUtils.updateApplicantInRefugeTab(finremCaseDetails);
        RefugeWrapperUtils.updateRespondentInRefugeTab(finremCaseDetails);

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
            .data(finremCaseData)
            .errors(getPostCodeErrors(finremCaseDetails))
            .build();
    }

    private void considerContestedMiniFormA(FinremCaseDetails finremCaseDetails,
                                            String userAuthorisation) {

        Optional<ContactDetailsWrapper> contactDetailsWrapper =
                Optional.ofNullable(finremCaseDetails.getData().getContactDetailsWrapper());

        YesOrNo isRespondentAddressHidden = contactDetailsWrapper
                .map(ContactDetailsWrapper::getRespondentAddressHiddenFromApplicant)
                .orElse(YesOrNo.NO);

        YesOrNo isApplicantAddressHidden = contactDetailsWrapper
                .map(ContactDetailsWrapper::getApplicantAddressHiddenFromRespondent)
                .orElse(YesOrNo.NO);

        if (isRespondentAddressHidden == YesOrNo.YES || isApplicantAddressHidden == YesOrNo.YES) {
            CaseDocument document = onlineFormDocumentService.generateContestedMiniForm(userAuthorisation, finremCaseDetails);
            finremCaseDetails.getData().setMiniFormA(document);
        }
    }

    /*
     * Repeat mid-event validation to protect Users navigating with browser controls.
     * @param finremCaseDetails case details
     * @return list of errors
     */
    private List<String> getPostCodeErrors(FinremCaseDetails finremCaseDetails) {
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        List<String> errors = new ArrayList<>();
        errors.addAll(internationalPostalService.validate(finremCaseData));
        errors.addAll(ContactDetailsValidator.validateCaseDataEmailAddresses(finremCaseData));

        if (CaseType.CONSENTED.equals(finremCaseDetails.getCaseType())) {
            errors.addAll(ContactDetailsValidator.validatePostcodesByRepresentation(finremCaseDetails));
        }

        if (CaseType.CONTESTED.equals(finremCaseDetails.getCaseType())) {
            errors.addAll(ContactDetailsValidator.validateCaseDataAddresses(finremCaseData));
        }

        return errors;
    }
}
