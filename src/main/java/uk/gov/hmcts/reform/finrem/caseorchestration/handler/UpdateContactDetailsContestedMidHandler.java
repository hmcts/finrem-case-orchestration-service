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

@Slf4j
@Service
public class UpdateContactDetailsContestedMidHandler extends FinremCallbackHandler {

    private final InternationalPostalService postalService;

    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";

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

        List<String> errors = new ArrayList<>();

        FinremCaseData caseData = caseDetails.getData();

        errors.addAll(postalService.validate(caseData));

        validateCaseData(caseData, errors);

        if (!errors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }

    private void validateCaseData(FinremCaseData caseData, List<String> errors) {

        if (caseData.isContestedApplication()) {
            ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();

            if (caseData.isApplicantRepresentedByASolicitor()) {
                if (wrapper.getApplicantSolicitorAddress() != null
                    && ObjectUtils.isEmpty(wrapper.getApplicantSolicitorAddress().getPostCode()))
                    errors.add(APPLICANT_POSTCODE_ERROR);
            } else {
                if (wrapper.getApplicantAddress() != null
                    && ObjectUtils.isEmpty(wrapper.getApplicantAddress().getPostCode()))
                    errors.add(APPLICANT_POSTCODE_ERROR);
            }

            if (caseData.isRespondentRepresentedByASolicitor()) {
                if (wrapper.getRespondentSolicitorAddress() != null
                    && ObjectUtils.isEmpty(wrapper.getRespondentSolicitorAddress().getPostCode()))
                    errors.add(RESPONDENT_POSTCODE_ERROR);
            } else {
                if (wrapper.getApplicantAddress() != null
                    && ObjectUtils.isEmpty(wrapper.getRespondentAddress().getPostCode()))
                    errors.add(RESPONDENT_POSTCODE_ERROR);
            }
        }
    }
}
