package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CourtDetailsConfigurationTest {

    @Test
    void testConstructor() throws IOException {
        CourtDetailsConfiguration config = new CourtDetailsConfiguration(new ObjectMapper());

        assertThat(config.getCourts()).hasSize(137);
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
}
