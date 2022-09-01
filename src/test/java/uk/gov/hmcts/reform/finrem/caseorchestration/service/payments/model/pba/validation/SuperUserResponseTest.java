package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class SuperUserResponseTest {

    private SuperUserResponse superUser;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\n"
            + "      \"firstName\": \"Henry\",\n"
            + "      \"lastName\": \"Harper\",\n"
            + "      \"email\": \"henry_fr_harper@yahoo.com\"\n"
            + "    },\n";

        superUser = mapper.readValue(json, SuperUserResponse.class);
    }

    @Test
    public void shouldPopulateData() {
        assertThat(superUser.getEmail(), Is.is("henry_fr_harper@yahoo.com"));
        assertThat(superUser.getFirstName(), Is.is("Henry"));
        assertThat(superUser.getLastName(), Is.is("Harper"));
    }
}