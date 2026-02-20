
package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignUserToCaseRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CitizenCaseAssignedRoleControllerTest {

    private AssignCaseAccessService assignCaseAccessService;
    private CitizenCaseAssignedRoleController controller;

    @BeforeEach
    void setUp() {
        assignCaseAccessService = Mockito.mock(AssignCaseAccessService.class);
        controller = new CitizenCaseAssignedRoleController(assignCaseAccessService);
    }

    @Test
    void shouldReturnOkAndCallServiceWhenValidRequest() {
        AssignUserToCaseRequest request = AssignUserToCaseRequest.builder()
            .caseId(12345L)
            .userId("user-1")
            .caseRole(CaseRole.APPLICANT)
            .build();

        ResponseEntity<Void> response = controller.assignCaseToUser("Bearer token", request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        verify(assignCaseAccessService, times(1)).grantCaseRoleToUser(
            eq(12345L),
            eq("user-1"),
            eq(CaseRole.APPLICANT.getCcdCode()),
            isNull()
        );
    }

    @Test
    void shouldWorkWhenAuthHeaderIsMissing() {
        AssignUserToCaseRequest request = AssignUserToCaseRequest.builder()
            .caseId(99999L)
            .userId("abc")
            .caseRole(CaseRole.APPLICANT)
            .build();

        ResponseEntity<Void> response = controller.assignCaseToUser(null, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        verify(assignCaseAccessService).grantCaseRoleToUser(
            99999L,
            "abc",
            CaseRole.APPLICANT.getCcdCode(),
            null
        );
    }

    @Test
    void shouldStillReturnOkEvenWhenAuthorizationHeaderIsEmpty() {
        AssignUserToCaseRequest request = AssignUserToCaseRequest.builder()
            .caseId(77777L)
            .userId("userX")
            .caseRole(CaseRole.RESPONDENT)
            .build();

        ResponseEntity<Void> response = controller.assignCaseToUser("", request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        verify(assignCaseAccessService).grantCaseRoleToUser(
            77777L,
            "userX",
            CaseRole.RESPONDENT.getCcdCode(),
            null
        );
    }

    @Test
    void shouldThrowExceptionWhenBodyIsNull() {
        assertThatThrownBy(() -> controller.assignCaseToUser("Bearer token", null))
            .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(assignCaseAccessService);
    }

    @Test
    void shouldThrowWhenCaseRoleIsNull() {
        AssignUserToCaseRequest request = AssignUserToCaseRequest.builder()
            .caseId(123L)
            .userId("user-1")
            .caseRole(null)
            .build();

        assertThatThrownBy(() -> controller.assignCaseToUser("Bearer", request))
            .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(assignCaseAccessService);
    }
}
