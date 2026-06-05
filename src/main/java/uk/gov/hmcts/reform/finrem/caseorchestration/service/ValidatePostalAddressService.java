package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidatePostalAddressService {

    private static final String ERROR_MESSAGE =
        "%s address details missing. Unable to complete %s until party address details are added to avoid failed postal notification.";

    public List<String> validateRequiredPostalAddresses(FinremCaseDetails caseDetails, EventType eventType) {
        FinremCaseData caseData = caseDetails.getData();
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        List<String> errors = new ArrayList<>();

        if (ContactDetailsValidator.checkForApplicantPostalAddress(wrapper)) {
            errors.add(String.format(ERROR_MESSAGE, "Applicant", eventType));
        }

        if (ContactDetailsValidator.checkForRespondentPostalAddress(wrapper)) {
            errors.add(String.format(ERROR_MESSAGE, "Respondent", eventType));
        }

        return errors;
    }
}
