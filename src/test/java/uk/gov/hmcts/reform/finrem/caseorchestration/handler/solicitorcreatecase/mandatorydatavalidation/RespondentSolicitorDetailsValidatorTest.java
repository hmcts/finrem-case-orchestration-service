package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RespondentSolicitorDetailsValidatorTest {

    @Test
    void shouldReturnErrorWhenApplicantAndRespondentHaveSameOrganisation() {
        String orgId = "ORG123";
        Organisation organisation = Organisation.builder().organisationID(orgId).build();
        OrganisationPolicy applicantPolicy = OrganisationPolicy.builder().organisation(organisation).build();
        OrganisationPolicy respondentPolicy = OrganisationPolicy.builder().organisation(organisation).build();

        FinremCaseData data = FinremCaseData.builder()
            .applicantOrganisationPolicy(applicantPolicy)
            .respondentOrganisationPolicy(respondentPolicy)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.YES)
                .contestedRespondentRepresented(YesOrNo.YES)
                .build())
            .build();

        List<String> errors = new RespondentSolicitorDetailsValidator().validate(data);

        assertThat(errors).contains("Applicant organisation cannot be the same as respondent organisation");
    }
}
