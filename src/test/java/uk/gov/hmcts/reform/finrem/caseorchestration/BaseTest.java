package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
public abstract class BaseTest {
    @MockBean protected AuthTokenGenerator authTokenGenerator;
    @MockBean protected IdamClient idamClient;
}
