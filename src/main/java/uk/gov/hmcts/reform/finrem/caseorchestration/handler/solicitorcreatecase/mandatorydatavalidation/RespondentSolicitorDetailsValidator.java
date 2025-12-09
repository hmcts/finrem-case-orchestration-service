package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.NullChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
class RespondentSolicitorDetailsValidator implements MandatoryDataValidator {

    @Override
    public List<String> validate(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        List<String> ret = new ArrayList<>();

        if (shouldSkipRespondentSolicitorValidation(contactDetailsWrapper)) {
            return ret;
        }

        checkRespondentOrganisationPolicy(caseData, ret);
        if (caseData.isConsentedApplication()) {
            if (contactDetailsWrapper.getRespondentSolicitorAddress() == null
                || !NullChecker.anyNonNull(contactDetailsWrapper.getRespondentSolicitorAddress())) {
                ret.add("Respondent solicitor's address is required.");
            }
            validateField(contactDetailsWrapper.getRespondentSolicitorEmail(), "email", ret);
            validateField(contactDetailsWrapper.getRespondentSolicitorPhone(), "phone", ret);
            validateField(contactDetailsWrapper.getRespondentSolicitorFirm(), "name of firm", ret);
            validateField(contactDetailsWrapper.getRespondentSolicitorName(), "name", ret);
        }
        return ret;
    }

    private boolean shouldSkipRespondentSolicitorValidation(ContactDetailsWrapper contactDetailsWrapper) {
        return YesOrNo.NO.equals(contactDetailsWrapper.getConsentedRespondentRepresented())
            || YesOrNo.NO.equals(contactDetailsWrapper.getContestedRespondentRepresented());
    }

    private void checkRespondentOrganisationPolicy(FinremCaseData caseData, List<String> errors) {
        if (Optional.ofNullable(caseData.getRespondentOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse(null) == null) {
            errors.add("Respondent organisation policy is missing.");
        }
    }

    private void validateField(String fieldValue, String fieldName, List<String> errors) {
        if (!StringUtils.hasText(fieldValue)) {
            errors.add(String.format("Respondent solicitor's %s is required.", fieldName));
        }
    }
}
