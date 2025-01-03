package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UpdateContactDetailsConsentedMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService postalService;
    private final UpdateContactDetailsService updateContactDetailsService;
    private final UpdateRepresentationWorkflowService nocWorkflowService;

    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    private static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    private static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";

    public UpdateContactDetailsConsentedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                   InternationalPostalService postalService,
                                                   UpdateContactDetailsService updateContactDetailsService,
                                                   UpdateRepresentationWorkflowService nocWorkflowService) {
        super(finremCaseDetailsMapper);
        this.postalService = postalService;
        this.updateContactDetailsService = updateContactDetailsService;
        this.nocWorkflowService = nocWorkflowService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking consented event {} mid event callback for case id {}", EventType.UPDATE_CONTACT_DETAILS,
            finremCaseDetails.getId());

        FinremCaseData caseData = finremCaseDetails.getData();
        List<String> errors = new ArrayList<>();
        errors.addAll(postalService.validate(caseData));
        errors.addAll(validateCaseData(caseData));

        Optional<ContactDetailsWrapper> contactDetailsWrapper = Optional.ofNullable(caseData.getContactDetailsWrapper());
        boolean includeRepresentationChange = contactDetailsWrapper
            .map(wrapper -> wrapper.getUpdateIncludesRepresentativeChange() == YesOrNo.YES)
            .orElse(false);

        if (includeRepresentationChange) {
            updateContactDetailsService.handleRepresentationChange(caseData, finremCaseDetails.getCaseType());
            CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
            CaseDetails caseDetailsBefore = finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetailsBefore());

            Map<String, Object> updateCaseData = nocWorkflowService
                .handleNoticeOfChangeWorkflow(caseDetails, userAuthorisation, caseDetailsBefore)
                .getData();

            caseData = finremCaseDetailsMapper.mapToFinremCaseData(updateCaseData, caseDetails.getCaseTypeId());
        } else {
            updateContactDetailsService.persistOrgPolicies(caseData, callbackRequest.getCaseDetailsBefore().getData());
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }


    private List<String> validateCaseData(FinremCaseData caseData) {

        List<String> errors = new ArrayList<>();
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();

        if (caseData.isApplicantRepresentedByASolicitor()
            && wrapper.getSolicitorAddress() != null
            && ObjectUtils.isEmpty(wrapper.getSolicitorAddress().getPostCode())) {
            errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            return errors;
        }

        if (wrapper.getApplicantAddress() != null
            && ObjectUtils.isEmpty(wrapper.getApplicantAddress().getPostCode())) {
            errors.add(APPLICANT_POSTCODE_ERROR);
            return errors;
        }

        if (caseData.isRespondentRepresentedByASolicitor()
            && wrapper.getRespondentSolicitorAddress() != null
            && ObjectUtils.isEmpty(wrapper.getRespondentSolicitorAddress().getPostCode())) {
            errors.add(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
            return errors;
        }

        if (wrapper.getRespondentAddress() != null
            && ObjectUtils.isEmpty(wrapper.getRespondentAddress().getPostCode())) {
            errors.add(RESPONDENT_POSTCODE_ERROR);
            return errors;
        }

        return errors;
    }
}

