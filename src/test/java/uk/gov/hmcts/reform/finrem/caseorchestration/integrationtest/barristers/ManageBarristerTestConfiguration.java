package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.barristers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.IdamAuthApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.AssignCaseAccessRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RestService;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers",
    "uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister",
    "uk.gov.hmcts.reform.finrem.caseorchestration.helper"})
public class ManageBarristerTestConfiguration {

    @Bean
    public CaseDataService caseDataService() {
        return new CaseDataService();
    }

    @MockBean
    private IdamAuthApi idamAuthApi;

    @MockBean
    private AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;

    @MockBean
    private AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;

    @MockBean
    private RestService restService;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @MockBean
    private NotificationService notificationService;
}
