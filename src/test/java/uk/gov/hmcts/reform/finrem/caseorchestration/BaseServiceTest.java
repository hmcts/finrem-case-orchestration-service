package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations = "/application.properties")
@DirtiesContext
public abstract class BaseServiceTest extends BaseTest {
}
