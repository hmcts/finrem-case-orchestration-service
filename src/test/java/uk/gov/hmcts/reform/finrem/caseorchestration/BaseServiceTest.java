package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations = "/application.properties")
@DirtiesContext
public abstract class BaseServiceTest extends BaseTest {

    @Autowired
    protected ObjectMapper mapper;
}
