package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.junit.Assert.assertEquals;

public class AssignCaseAccessServiceConfigurationTest extends BaseServiceTest {
    @Autowired private AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;

    @Test
    public void shouldReturnTheConfiguration() {
        assertEquals("http://localhost:4454/case-assignments", assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl());
    }
}