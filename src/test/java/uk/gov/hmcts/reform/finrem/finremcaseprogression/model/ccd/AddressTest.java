package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AddressTest {
    Address address;

    @Before
    public void setUp() throws Exception {
        String json = "{"
                + " \"AddressLine1\" : \"Unit 14\", "
                + " \"AddressLine2\" : \"3 Edgar Buildings\", "
                + " \"AddressLine3\" : \"George Street\", "
                + " \"PostTown\" : \"Bath\", "
                + " \"County\" : \"Somerset\", "
                + " \"PostCode\" : \"BA1 2FJ\", "
                + " \"Country\" : \"England\" "
                + "} ";
        ObjectMapper mapper = new ObjectMapper();
        address = mapper.readValue(json, Address.class);
    }

    @Test
    public void shouldCreateAddressFromJson() {
        assertThat(address.getAddressLine1(), is("Unit 14"));
        assertThat(address.getAddressLine2(), is("3 Edgar Buildings"));
        assertThat(address.getAddressLine3(), is("George Street"));
        assertThat(address.getPostTown(), is("Bath"));
        assertThat(address.getCounty(), is("Somerset"));
        assertThat(address.getPostCode(), is("BA1 2FJ"));
        assertThat(address.getCountry(), is("England"));
    }
}