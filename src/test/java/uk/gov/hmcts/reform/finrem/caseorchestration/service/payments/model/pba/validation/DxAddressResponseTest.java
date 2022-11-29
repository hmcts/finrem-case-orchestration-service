package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DxAddressResponseTest {

    private DxAddressResponse dxAddress;

    @Before
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"dxNumber\":\"Dx001\","
            + "\"dxExchange\":\"xxxxxx\" }";
        dxAddress = objectMapper.readValue(json, DxAddressResponse.class);
    }

    @Test
    public void shouldPopulateData() {
        assertThat(dxAddress.getDxNumber(), is("Dx001"));
        assertThat(dxAddress.getDxExchange(), is("xxxxxx"));
    }
}