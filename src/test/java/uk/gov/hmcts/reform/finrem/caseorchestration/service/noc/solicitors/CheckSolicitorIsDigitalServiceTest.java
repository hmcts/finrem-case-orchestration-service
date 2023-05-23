package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@RunWith(MockitoJUnitRunner.class)
public class CheckSolicitorIsDigitalServiceTest {

    private static final String CASE_ID = "1234567890";

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @InjectMocks
    private CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;


    @Test
    public void givenApplicantSolicitorIsDigital_whenIsApplicantSolicitorDigital_thenReturnTrue() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(caseAssignmentUserRoles(APP_SOLICITOR_POLICY));

        assertTrue(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(CASE_ID));
    }

    @Test
    public void givenApplicantSolicitorIsNotDigital_whenIsApplicantSolicitorDigital_thenReturnFalse() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(caseAssignmentUserRoles(RESP_SOLICITOR_POLICY));

        assertFalse(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(CASE_ID));
    }

    @Test
    public void givenRespondentSolicitorIsDigital_whenIsRespondentSolicitorDigital_thenReturnTrue() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(caseAssignmentUserRoles(RESP_SOLICITOR_POLICY));

        assertTrue(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(CASE_ID));
    }

    @Test
    public void givenRespondentSolicitorIsNotDigital_whenIsRespondentSolicitorDigital_thenReturnFalse() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(caseAssignmentUserRoles(APP_SOLICITOR_POLICY));

        assertFalse(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(CASE_ID));
    }

    @Test
    public void givenIntervenerSolicitorIsDigital_whenIsIntervenerSolicitorDigital_thenReturnTrue() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(caseAssignmentUserRoles(CaseRole.INTVR_SOLICITOR_1.getValue()));

        assertTrue(checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(CASE_ID, CaseRole.INTVR_SOLICITOR_1.getValue()));
    }

    @Test
    public void givenIntervenerSolicitorIsNotDigital_whenIsRespondentSolicitorDigital_thenReturnFalse() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(caseAssignmentUserRoles(APP_SOLICITOR_POLICY));

        assertFalse(checkSolicitorIsDigitalService.isIntervenerSolicitorDigital(CASE_ID, CaseRole.INTVR_SOLICITOR_1.getValue()));
    }

    @Test
    public void givenDataStoreResponseIsEmpty_whenIsSolicitorDigital_thenReturnFalse() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(Collections.emptyList()).build());

        assertFalse(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(CASE_ID));
        assertFalse(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(CASE_ID));
    }

    @Test
    public void givenDataStoreResponseIsNull_whenIsSolicitorDigital_thenReturnFalse() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(null);

        assertFalse(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(CASE_ID));
        assertFalse(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(CASE_ID));
    }

    @Test
    public void givenResponseListIsNull_whenIsSolicitorDigital_thenReturnFalse() {
        when(assignCaseAccessService.searchUserRoles(CASE_ID)).thenReturn(CaseAssignmentUserRolesResource.builder().build());

        assertFalse(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(CASE_ID));
        assertFalse(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(CASE_ID));
    }

    private CaseAssignmentUserRolesResource caseAssignmentUserRoles(String caseRole) {
        return CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder()
                    .caseRole(caseRole)
                    .build()))
            .build();
    }
}