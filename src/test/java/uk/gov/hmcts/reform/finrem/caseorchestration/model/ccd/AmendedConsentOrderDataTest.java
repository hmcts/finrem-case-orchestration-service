package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class AmendedConsentOrderDataTest  extends  AmendedConsentOrderTest {
    private AmendedConsentOrderData orderData;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        orderData = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/amended-consent-order-data.json").toURI()),
                AmendedConsentOrderData.class);
        order = orderData.getAmendedConsentOrder();
    }

    @Test
    public void shouldCreateAmendedConsentOrderDataFromJson() {
        assertThat(orderData.getId(), is("1"));
        assertThat(orderData.getAmendedConsentOrder(), is(notNullValue()));
    }
}