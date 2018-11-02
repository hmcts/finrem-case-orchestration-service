package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OrderRefusalDataTest extends OrderRefusalTest {
    OrderRefusalData orderData;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        orderData = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/order-refusal-data.json").toURI()), OrderRefusalData.class);
        order = orderData.getOrderRefusal();
    }

    @Test
    public void shouldCreateOrderRefusalDataFromJson() {
        assertThat(orderData.getId(), is("1"));
    }

}