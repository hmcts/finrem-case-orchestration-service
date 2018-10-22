package uk.gov.hmcts.reform.finrem.finremcaseprogression.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.FinremCaseProgressionApplication;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FinremCaseProgressionApplication.class)
@TestPropertySource(locations = "/application.properties")
public class FeeServiceConfigurationTest {

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