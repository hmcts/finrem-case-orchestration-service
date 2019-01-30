package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FeeResponseServiceConfigurationTest extends BaseServiceTest {

    @Autowired
    private FeeServiceConfiguration config;

    @Test
    public void shouldCreateFeeServiceConfigFromAppProperties() {
        assertThat(config.getUrl(), is("http://test"));
        assertThat(config.getApi(), is("/api"));
        assertThat(config.getChannel(), is("default"));
        assertThat(config.getEvent(), is("general-application"));
        assertThat(config.getJurisdiction1(), is("family"));
        assertThat(config.getJurisdiction2(), is("family-court"));
        assertThat(config.getKeyword(), is("without-notice"));
        assertThat(config.getService(), is("other"));
    }

}