package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionHighCourtFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;

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

    private FinremCaseDetails finremCaseDetails;

    @BeforeEach
    void setUp() {
        finremCaseDetails = FinremCaseDetails.builder()
            .id(1L)
            .data(
                FinremCaseData.builder()
                    .regionWrapper(
                        RegionWrapper.builder()
                            .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                                .build())
                            .build())
                    .build())
            .build();
    }

    @Test
    void returnsStandardTemplate_whenHighCourtNotSelected() {
        assertThat(documentConfiguration.getVacateHearingNoticeTemplate(finremCaseDetails))
            .isEqualTo("FL-FRM-HNO-ENG-00024.docx");
    }

    @Test
    void returnsHighCourtTemplate_whenHighCourtSelected() {
        finremCaseDetails.getData().getRegionWrapper().getAllocatedRegionWrapper().setHighCourtFrcList(RegionHighCourtFrc.HIGHCOURT);
        assertThat(documentConfiguration.getVacateHearingNoticeTemplate(finremCaseDetails))
            .isEqualTo("FL-FRM-HNO-ENG-00025.docx");
    }
}
