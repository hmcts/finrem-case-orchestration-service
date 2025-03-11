package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;

@ExtendWith(MockitoExtension.class)
class SelectedCourtServiceTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(SelectedCourtService.class);

    private SelectedCourtService selectedCourtService;
    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    @BeforeEach
    void setUp() {
        selectedCourtService = new SelectedCourtService(courtDetailsConfiguration);
    }

    /*
     * The courtDetailsConfiguration is set up to return test data when asked for information about
     * a court with the id FR_s_NottinghamList_7.
     *
     * defaultContestedFinremCaseDetails is used because the case it builds includes a court list for Nottingham
     * containing court FR_s_NottinghamList_7.
    */
    @Test
    void shouldSetSelectedCourtDetailsWhenCourtExists() {

        Map<String, CourtDetails> courtDetailsMap = Map.of(
                "FR_s_NottinghamList_7", CourtDetails.builder().email("court@example.com")
                        .courtAddress("Court address")
                        .courtName("A court name")
                        .phoneNumber("01111 11111111").build()
        );

        when(courtDetailsConfiguration.getCourts()).thenReturn(courtDetailsMap);

        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();

        selectedCourtService.setSelectedCourtDetailsIfPresent(caseDetails.getData());

        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcName()).isEqualTo("A court name");
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcAddress()).isEqualTo("Court address");
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcEmail()).isEqualTo("court@example.com");
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcPhone()).isEqualTo("01111 11111111");

    }

    /*
     * The courtDetailsConfiguration is set up to return null when asked for information about
     * a court with the id FR_s_NottinghamList_7.
     *
     * defaultContestedFinremCaseDetails is used because the case it builds includes a court list for Nottingham
     * containing court FR_s_NottinghamList_7.
     *
     * Test confirms that no metrics data is set, but that a Warning is logged.
     */
    @Test
    void shouldNotSetSelectedCourtDetailsWhenCourtDataEmpty() {

        Map<String, CourtDetails> courtDetailsMap = Map.of(
                "FR_s_NottinghamList_7", CourtDetails.builder().build()
        );

        when(courtDetailsConfiguration.getCourts()).thenReturn(courtDetailsMap);

        FinremCaseDetails caseDetails = defaultContestedFinremCaseDetails();

        selectedCourtService.setSelectedCourtDetailsIfPresent(caseDetails.getData());

        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcName()).isNull();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcAddress()).isNull();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcEmail()).isNull();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcPhone()).isNull();

        assertThat(logs.getWarns()).contains(
                "Warning: FR_s_NottinghamList_7 is missing a value for Court Name so the consentOrderFRC value will not be set.",
                "Warning: FR_s_NottinghamList_7 is missing a value for Court Address so the consentOrderFRC value will not be set.",
                "Warning: FR_s_NottinghamList_7 is missing a value for Court Email so the consentOrderFRC value will not be set.",
                "Warning: FR_s_NottinghamList_7 is missing a value for Court Phone so the consentOrderFRC value will not be set."
        );
    }

    /*
     * Test that setSelectedCourtDetailsIfPresent does nothing when the region hasn't been chosen yet.
     * This matches the situation when the method is used before any court is chosen.
     *
     * Test confirms that no fields are set and no warnings are logged.
     */
    @Test
    void shouldNotSetSelectedCourtDetailsWhenNoCourtSelected() {

        FinremCaseDetails caseDetails = new FinremCaseDetails();

        caseDetails.setData(FinremCaseData.builder().build());

        selectedCourtService.setSelectedCourtDetailsIfPresent(caseDetails.getData());

        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcName()).isNull();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcAddress()).isNull();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcEmail()).isNull();
        assertThat(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcPhone()).isNull();

        assertThat(logs.getWarns()).isEmpty();
    }

    /*
     * Test that royalCourtOrHighCourtChosen does the following:
     * Returns true if "High Court Family Division" or "The Royal Court of Justice" are chosen
     * Returns false if another court is chosen
     * Returns false if a court has not been selected yet.
     *
     * Test confirms that no fields are set and no warnings are logged.
     */
    @Test
    void royalCourtOrHighCourtChosenHandlesCourtsAndNull() {

        FinremCaseDetails caseDetails = new FinremCaseDetails();

        caseDetails.setData(FinremCaseData.builder().build());
        FinremCaseData finremCaseData = caseDetails.getData();

        finremCaseData.getConsentOrderWrapper().setConsentOrderFrcName("High Court Family Division");
        assertThat(selectedCourtService.royalCourtOrHighCourtChosen(caseDetails.getData())).isTrue();

        finremCaseData.getConsentOrderWrapper().setConsentOrderFrcName("The Royal Courts of Justice");
        assertThat(selectedCourtService.royalCourtOrHighCourtChosen(caseDetails.getData())).isTrue();

        finremCaseData.getConsentOrderWrapper().setConsentOrderFrcName("Central Family Court");
        assertThat(selectedCourtService.royalCourtOrHighCourtChosen(caseDetails.getData())).isFalse();

        finremCaseData.getConsentOrderWrapper().setConsentOrderFrcName(null);
        assertThat(selectedCourtService.royalCourtOrHighCourtChosen(caseDetails.getData())).isFalse();
    }
}
