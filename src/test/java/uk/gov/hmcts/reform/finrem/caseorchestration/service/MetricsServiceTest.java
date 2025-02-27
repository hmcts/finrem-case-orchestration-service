package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsService(courtDetailsConfiguration);
    }

    /*
     * The courtDetailsConfiguration is set up to return test data when asked for information about
     * a court with the id FR_s_NottinghamList_7.
     *
     * defaultContestedFinremCaseDetails is used because the case it builds includes a court list for Nottingham
     * containing court FR_s_NottinghamList_7.
    */
    @Test
    void shouldSetCourtMetricsWhenCourtExists() {

        Map<String, CourtDetails> courtDetailsMap = Map.of(
                "FR_s_NottinghamList_7", CourtDetails.builder().email("court@example.com")
                        .courtAddress("Court address")
                        .courtName("A court name")
                        .phoneNumber("01111 11111111").build()
        );

        when(courtDetailsConfiguration.getCourts()).thenReturn(courtDetailsMap);

        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();

        metricsService.setCourtMetrics(caseDetails.getData());

        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcName()).isEqualTo("A court name");
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcAddress()).isEqualTo("Court address");
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcEmail()).isEqualTo("court@example.com");
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcPhone()).isEqualTo("01111 11111111");

    }

    /*
     * The courtDetailsConfiguration is set up to return EMPTY data when asked for information about
     * a court with the id FR_s_NottinghamList_7.
     *
     * defaultContestedFinremCaseDetails is used because the case it builds includes a court list for Nottingham
     * containing court FR_s_NottinghamList_7.
     */
    @Test
    void shouldSetCourtMetricsWhenCourtDataEmpty() {

        Map<String, CourtDetails> courtDetailsMap = Map.of(
                "FR_s_NottinghamList_7", CourtDetails.builder().email("")
                        .courtAddress("")
                        .courtName("")
                        .phoneNumber("").build()
        );

        when(courtDetailsConfiguration.getCourts()).thenReturn(courtDetailsMap);

        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();

        metricsService.setCourtMetrics(caseDetails.getData());

        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcName()).isEmpty();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcAddress()).isEmpty();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcEmail()).isEmpty();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcPhone()).isEmpty();
    }
}
