package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = DocumentConfigurationTest.TestConfig.class)
@TestPropertySource(locations = "file:src/main/resources/application.properties")
class DocumentConfigurationTest {

    /*
     * registers DocumentConfiguration as a @ConfigurationProperties bean
     */
    @Configuration
    @EnableConfigurationProperties(DocumentConfiguration.class)
    static class TestConfig {
    }

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private DocumentConfiguration documentConfiguration;

    @BeforeEach
    void setup() {
        when(featureToggleService.isFinremCitizenUiEnabled()).thenReturn(false);
    }

    @Test
    void returnsStandardTemplate_whenHighCourtNotSelected() {
        assertThat(documentConfiguration.getVacateOrAdjournNoticeTemplate(Region.SOUTHWEST))
            .isEqualTo("FL-FRM-HNO-ENG-00025.docx");
    }

    @Test
    void returnsHighCourtTemplate_whenHighCourtSelected() {
        assertThat(documentConfiguration.getVacateOrAdjournNoticeTemplate(Region.HIGHCOURT))
            .isEqualTo("FL-FRM-HNO-ENG-00024.docx");
    }
}
