package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.createcase;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation"})
public class CreateCaseMandatoryDataTestConfiguration {

    @MockBean
    OnlineFormDocumentService onlineFormDocumentService;

    @MockBean
    CaseFlagsService caseFlagsService;

    @MockBean
    IdamService idamService;
    @MockBean
    UpdateRepresentationWorkflowService updateRepresentationWorkflowService;
}
