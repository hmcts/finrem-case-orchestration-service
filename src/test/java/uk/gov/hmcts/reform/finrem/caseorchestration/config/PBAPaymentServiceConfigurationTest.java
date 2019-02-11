package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PBAPaymentServiceConfigurationTest extends BaseServiceTest {

    @Autowired
    private PBAPaymentServiceConfiguration config;

    @Test
    public void shouldCreatePaymentByAccountServiceConfigFromAppProperties() {
        assertThat(config.getUrl(), is("http://test"));
        assertThat(config.getApi(), is("/credit-account-payments"));
        assertThat(config.getDescription(), is("Financial Remedy Consented Application"));
        assertThat(config.getSiteId(), is("AA03"));
    }
}