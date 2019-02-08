package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class OrderRefusalDataTest extends OrderRefusalTest {
    OrderRefusalData orderData;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        orderData = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/prod/order-refusal-data.json").toURI()), OrderRefusalData.class);
        order = orderData.getOrderRefusal();
    }

    @Test
    public void shouldCreateOrderRefusalDataFromJson() {
        assertThat(orderData.getId(), is("1"));
        assertThat(orderData.getOrderRefusal(), is(notNullValue()));
    }

}