package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UpdateContactDetailsContestedMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService postalService;

    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    private static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    private static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";

    public UpdateContactDetailsContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                   InternationalPostalService postalService) {
        super(finremCaseDetailsMapper);
        this.postalService = postalService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested event {} mid event callback for case id {}", EventType.UPDATE_CONTACT_DETAILS,
            caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();
        errors.addAll(postalService.validate(caseData));
        errors.addAll(validateCaseData(caseData));
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }

    private List<String> validateCaseData(FinremCaseData caseData) {
        List<String> errors = new ArrayList<>();
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();

        if (caseData.isApplicantRepresentedByASolicitor()
            && Optional.ofNullable(wrapper.getApplicantSolicitorAddress())
                .map(address -> ObjectUtils.isEmpty(address.getPostCode()))
                .orElse(false)) {
            errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            return errors;
        }

        if (Optional.ofNullable(wrapper.getApplicantAddress())
            .map(address -> ObjectUtils.isEmpty(address.getPostCode()))
            .orElse(false)) {
            errors.add(APPLICANT_POSTCODE_ERROR);
            return errors;
        }

        if (caseData.isRespondentRepresentedByASolicitor()
            && Optional.ofNullable(wrapper.getRespondentSolicitorAddress())
                .map(address -> ObjectUtils.isEmpty(address.getPostCode()))
                .orElse(false)) {
            errors.add(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
            return errors;
        }

        if (!caseData.isRespondentRepresentedByASolicitor() && Optional.ofNullable(wrapper.getRespondentAddress())
            .map(address -> ObjectUtils.isEmpty(address.getPostCode()))
            .orElse(false)) {
            errors.add(RESPONDENT_POSTCODE_ERROR);
            return errors;
        }

        return errors;
    }
}
