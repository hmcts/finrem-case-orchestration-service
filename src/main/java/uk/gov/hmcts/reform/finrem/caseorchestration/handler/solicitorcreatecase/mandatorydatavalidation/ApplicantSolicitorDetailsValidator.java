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
public class ApplicantSolicitorDetailsValidator implements MandatoryDataValidator {

    @Override
    public List<String> validate(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();

        List<String> ret = new ArrayList<>();

        if (shouldSkipSolicitorValidation(contactDetailsWrapper, caseData, ret)) {
            return ret;
        }

        checkOrganisationPolicy(caseData, ret);
        if (caseData.isConsentedApplication()) {
            if (contactDetailsWrapper.getSolicitorAddress() == null
                || !NullChecker.anyNonNull(contactDetailsWrapper.getSolicitorAddress())) {
                ret.add("Applicant solicitor's address is required.");
            }
            validateField(contactDetailsWrapper.getSolicitorEmail(), "email", ret);
            validateField(contactDetailsWrapper.getSolicitorPhone(), "phone", ret);
            validateField(contactDetailsWrapper.getSolicitorFirm(), "name of your firm", ret);
            validateField(contactDetailsWrapper.getSolicitorName(), "name", ret);
        }
        return ret;
    }

    private boolean shouldSkipSolicitorValidation(ContactDetailsWrapper contactDetailsWrapper, FinremCaseData caseData, List<String> errors) {
        if (YesOrNo.NO.equals(contactDetailsWrapper.getApplicantRepresented())) {
            log.info("{} - Skip validating solicitor details since the applicant is not represented", caseData.getCcdCaseId());
            return true;
        }
        return false;
    }

    private void checkOrganisationPolicy(FinremCaseData caseData, List<String> errors) {
        if (Optional.ofNullable(caseData.getApplicantOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse(null) == null) {
            errors.add("Applicant organisation policy is missing.");
        }
    }

    private void validateField(String fieldValue, String fieldName, List<String> errors) {
        if (!StringUtils.hasText(fieldValue)) {
            errors.add(String.format("Applicant solicitor's %s is required.", fieldName));
        }
    }
}
