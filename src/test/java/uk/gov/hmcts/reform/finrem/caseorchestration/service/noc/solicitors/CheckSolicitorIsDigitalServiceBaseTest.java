package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@RunWith(MockitoJUnitRunner.class)
public class CheckSolicitorIsDigitalServiceBaseTest {

    private final CheckSolicitorIsDigitalServiceBase checkSolicitorIsDigitalServiceBase = Mockito.mock(
        CheckSolicitorIsDigitalServiceBase.class,
        Mockito.CALLS_REAL_METHODS);

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        caseData.put(APPLICANT_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID("ORG1234").organisationName("TestName").build())
            .build());
        caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).id(123L).data(caseData).build();
    }

    @Test
    public void givenOrganisationIsEmpty_whenIsOrganisationIdEmpty_thenReturnTrue() {
        OrganisationPolicy organisationPolicy = getOrganisationPolicy();
        Organisation organisation = new Organisation();

        organisation.setOrganisationID(null);
        organisationPolicy.setOrganisation(organisation);

        boolean isOrganisationEmpty = checkSolicitorIsDigitalServiceBase.isOrganisationEmpty(organisationPolicy);

        assertTrue(isOrganisationEmpty);
    }

    @Test
    public void givenOrganisationIsEmpty_whenIsOrganisationNameEmpty_thenReturnTrue() {
        OrganisationPolicy organisationPolicy = getOrganisationPolicy();
        Organisation organisation = new Organisation();

        organisation.setOrganisationName(null);
        organisationPolicy.setOrganisation(organisation);

        boolean isOrganisationEmpty = checkSolicitorIsDigitalServiceBase.isOrganisationEmpty(organisationPolicy);

        assertTrue(isOrganisationEmpty);
    }


    @Test
    public void givenOrganisationIsNotEmpty_whenIsOrganisationEmpty_thenReturnFalse() {
        OrganisationPolicy organisationPolicy = getOrganisationPolicy();

        boolean isOrganisationEmpty = checkSolicitorIsDigitalServiceBase.isOrganisationEmpty(organisationPolicy);

        assertFalse(isOrganisationEmpty);
    }

    @Test
    public void givenOrganisationIdIsNull_whenIsOrganisationIdValid_thenReturnFalse() {
        OrganisationPolicy organisationPolicy = getOrganisationPolicy();

        organisationPolicy.getOrganisation().setOrganisationID(null);

        boolean isValidOrganisationId = checkSolicitorIsDigitalServiceBase.isOrganisationIdRegistered(organisationPolicy);

        assertFalse(isValidOrganisationId);
    }

    @Test
    public void givenOrganisationIdIsEmpty_whenIsOrganisationIdValid_thenReturnFalse() {
        OrganisationPolicy organisationPolicy = getOrganisationPolicy();

        organisationPolicy.getOrganisation().setOrganisationID("");

        boolean isValidOrganisationId = checkSolicitorIsDigitalServiceBase.isOrganisationIdRegistered(organisationPolicy);

        assertFalse(isValidOrganisationId);
    }

    @Test
    public void givenOrganisationIdIsValid_whenIsOrganisationIdValid_thenReturnTrue() {
        OrganisationPolicy organisationPolicy = getOrganisationPolicy();

        boolean isValidOrganisationId = checkSolicitorIsDigitalServiceBase.isOrganisationIdRegistered(organisationPolicy);

        assertTrue(isValidOrganisationId);
    }

    public OrganisationPolicy getOrganisationPolicy() {
        Map<String, Object> caseData = caseDetails.getData();
        return new ObjectMapper().convertValue(caseData.get(APPLICANT_ORGANISATION_POLICY),
            OrganisationPolicy.class);
    }
}
