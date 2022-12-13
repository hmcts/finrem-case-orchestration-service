package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssignApplicantSolicitorServiceTest  {

    @Autowired
    private AssignApplicantSolicitorService assignApplicantSolicitorService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    public void shouldAssignApplicantSolicitor() {
        when(featureToggleService.isAssignCaseAccessEnabled()).thenReturn(true);


    }
}
