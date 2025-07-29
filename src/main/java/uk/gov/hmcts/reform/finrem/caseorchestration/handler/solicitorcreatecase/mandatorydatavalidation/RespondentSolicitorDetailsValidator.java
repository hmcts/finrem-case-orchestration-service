package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class RespondentSolicitorDetailsValidator implements MandatoryDataValidator {

    @Override
    public List<String> validate(FinremCaseData caseData) {
        List<String> errors = new ArrayList<>();
        checkApplicantAndRespondentHasSameSolicitor(caseData, errors);
        return errors;
    }

    /**
     * Check if the applicant organisation and respondent organisation are the same if the respondent is represented.
     * @param caseData
     * @param errors
     */
    private void checkApplicantAndRespondentHasSameSolicitor(FinremCaseData caseData, List<String> errors) {
        Optional<String> applicantOrgId = Optional.ofNullable(getApplicantOrgId(caseData));
        Optional<String> respondentOrgId = Optional.ofNullable(getRespondentOrgId(caseData));

        if (caseData.isApplicantRepresentedByASolicitor()
            && caseData.isRespondentRepresentedByASolicitor()
            && applicantOrgId.isPresent()
            && applicantOrgId.equals(respondentOrgId)) {
            errors.add("Applicant organisation cannot be the same as respondent organisation");
        }
    }

    private String getApplicantOrgId(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getApplicantOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse(null);
    }

    private String getRespondentOrgId(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getRespondentOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse(null);
    }
}
