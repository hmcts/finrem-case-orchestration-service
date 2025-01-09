package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AmendApplicationConsentedMidHandler extends FinremCallbackHandler {
    private final ConsentOrderService consentOrderService;
    private final InternationalPostalService postalService;
    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    private static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    private static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";
    private final ObjectMapper objectMapper;

    public AmendApplicationConsentedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               ConsentOrderService consentOrderService,
                                               InternationalPostalService postalService,
                                               ObjectMapper objectMapper) {
        super(finremCaseDetailsMapper);
        this.consentOrderService = consentOrderService;
        this.postalService = postalService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.AMEND_APP_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        log.info("Invoking amend application mid event for caseId {}", callbackRequest.getCaseDetails().getId());
        List<String> errors = consentOrderService.performCheck(objectMapper.convertValue(callbackRequest, CallbackRequest.class), userAuthorisation);
        List<String> validate = postalService.validate(callbackRequest.getCaseDetails().getData());
        errors.addAll(validate);

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();
        errors.addAll(validateCaseData(caseData));

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

