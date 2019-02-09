package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class PBAValidationServiceConfigurationTest extends BaseServiceTest {

    @Autowired
    private PBAValidationServiceConfiguration config;

    @Test
    public void shouldCreatePaymentByAccountServiceConfigFromAppProperties() {
        assertThat(config.getUrl(), is("http://test"));
        assertThat(config.getApi(), is("/case-orchestration/organisations/pba/"));
    }
}