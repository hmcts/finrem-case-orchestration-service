package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Test configuration explicitly refers to application.properties.
 * This is to confirm that template aliases and variables match.
 * A location of "classpath:application.properties" can be used, but points to the
 * application.properties file for tests.  So there is a risk when 'test' application.properties version is outdated.
 */
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "file:src/main/resources/application.properties")
@Import(DocumentConfigurationTest.TestConfig.class)
class DocumentConfigurationTest {

    /*
     * registers DocumentConfiguration as a @ConfigurationProperties bean
     */
    @Configuration
    @EnableConfigurationProperties(DocumentConfiguration.class)
    static class TestConfig {
    }

    @Autowired
    private DocumentConfiguration documentConfiguration;

    @Test
    void returnsStandardTemplate_whenHighCourtNotSelected() {
        assertThat(documentConfiguration.getVacateOrAdjournNoticeTemplate(Region.SOUTHWEST))
            .isEqualTo("FL-FRM-HNO-ENG-00024.docx");
    }

    @Test
    void returnsHighCourtTemplate_whenHighCourtSelected() {
        assertThat(documentConfiguration.getVacateOrAdjournNoticeTemplate(Region.HIGHCOURT))
            .isEqualTo("FL-FRM-HNO-ENG-00025.docx");
    }
}
