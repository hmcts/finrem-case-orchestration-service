package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RespondentSolicitorDetailsValidator implements MandatoryDataValidator {

    /**
     * Validates the given FinremCaseData for any errors related to Respondent Solicitor details.
     *
     * @param caseData The FinremCaseData to validate.
     * @return A list of error messages. An empty list indicates no errors were found.
     */
    @Override
    public List<String> validate(FinremCaseData caseData) {
        List<String> errors = new ArrayList<>();
        checkApplicantAndRespondentHasSameSolicitor(caseData, errors);
        return errors;
    }

    /**
     * Validates that the applicant and respondent have different organisations if both are represented by solicitors.
     * If they are the same, an error message is added to the errors list.
     *
     * @param caseData The FinremCaseData to validate.
     * @param errors   The list to which error messages will be added.
     */
    private void checkApplicantAndRespondentHasSameSolicitor(FinremCaseData caseData, List<String> errors) {
        if (caseData.isApplicantRepresentedByASolicitor() && caseData.isRespondentRepresentedByASolicitor()) {
            Optional<String> applicantOrgId = Optional.ofNullable(getApplicantOrgId(caseData));
            Optional<String> respondentOrgId = Optional.ofNullable(getRespondentOrgId(caseData));
            if (applicantOrgId.isPresent() && applicantOrgId.equals(respondentOrgId)) {
                errors.add("Applicant organisation cannot be the same as respondent organisation");
            }
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
