package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PBAValidationResponseTest {
    PBAValidationResponse response;

    @Before
    public void setUp() throws Exception {
        String json = "{ \"pbaNumberValid\" : true}";
        ObjectMapper mapper = new ObjectMapper();
        response = mapper.readValue(json, PBAValidationResponse.class);
    }

    @Test
    public void shouldCreatePBAValidationResponseFromJson() {
        assertThat(response.isPbaNumberValid(), is(true));
    }

}