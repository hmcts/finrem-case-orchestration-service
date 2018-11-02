package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class PaymentByAccountServiceConfigurationTest {

    @Autowired
    private PaymentByAccountServiceConfiguration config;


    @Test
    public void shouldCreatePaymentByAccountServiceConfigFromAppProperties() {
        assertThat(config.getUrl(), is("http://test"));
        assertThat(config.getApi(), is("/case-orchestration/organisations/pba/"));
    }
}