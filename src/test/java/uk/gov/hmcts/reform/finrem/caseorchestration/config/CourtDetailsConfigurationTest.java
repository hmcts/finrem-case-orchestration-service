package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManchesterCourt.CONSENTED_MANCHESTER_COURT;

class CourtDetailsConfigurationTest {

    @Test
    void testConstructor() throws IOException {
        CourtDetailsConfiguration config = new CourtDetailsConfiguration(new ObjectMapper());

        config.getCourts().forEach((k,v) -> {
            assertThat(k).isNotNull();
            assertThat(v).isNotNull();
            assertThat(v.getEmail()).isNotNull();
        });
        CourtDetails courtDetails = config.getCourts().get("FR_s_CFCList_1");
        assertThat(courtDetails.getCourtName()).isEqualTo("Bromley County Court And Family Court");
        assertThat(courtDetails.getCourtAddress()).isEqualTo("Bromley County Court, College Road, Bromley, BR1 3PX");
        assertThat(courtDetails.getPhoneNumber()).isEqualTo("0300 123 5577");
        assertThat(courtDetails.getEmail()).isEqualTo("FRCLondon@justice.gov.uk");
    }

    @Test
    void givenMappedConsentedCourt_whenGetCourtDetails_thenReturnCourtDetails() throws IOException {
        CourtDetailsConfiguration config = new CourtDetailsConfiguration(new ObjectMapper());

        CourtDetails courtDetails = config.getCourts().get(CONSENTED_MANCHESTER_COURT.getId());

        assertThat(courtDetails.getCourtName()).isEqualTo("Manchester County And Family Court");
        assertThat(courtDetails.getCourtAddress()).isEqualTo("1 Bridge Street West, Manchester, M60 9DJ");
        assertThat(courtDetails.getPhoneNumber()).isEqualTo("0300 123 5577");
        assertThat(courtDetails.getEmail()).isEqualTo("manchesterdivorce@justice.gov.uk");
    }
}
