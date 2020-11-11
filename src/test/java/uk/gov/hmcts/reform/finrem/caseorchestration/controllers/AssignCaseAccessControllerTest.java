package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(AssignCaseAccessController.class)
public class AssignCaseAccessControllerTest extends BaseControllerTest {

    @Autowired private AssignCaseAccessController assignCaseAccessController;

    @MockBean private AssignCaseAccessService assignCaseAccessService;

    @Test
    public void assignApplicantSolicitorCaseRole() {
        assignCaseAccessController.assignApplicantSolicitorCaseRole(AUTH_TOKEN, buildCallbackRequest());

        Mockito.verify(assignCaseAccessService).assignCaseAccess(any(CaseDetails.class), eq(AUTH_TOKEN));
    }
}