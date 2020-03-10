package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class CCDFeeCallbackResponseTest {

    private CCDFeeCallbackResponse ccdResponse;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ccdResponse = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/fee/ccd-fee-callback-response.json").toURI()),
                CCDFeeCallbackResponse.class);
    }

    @Test
    public void shouldCreateCCDFeeCallbackResponse() {
        assertThat(ccdResponse.getData().getAmountToPay(), is("1000"));
        assertThat(ccdResponse.getData().getOrderSummary().getPaymentReference(), is("Ref1"));
        assertThat(ccdResponse.getData().getOrderSummary().getPaymentTotal(), is("1000"));
        assertThat(ccdResponse.getData().getOrderSummary().getFees().size(), is(1));

        FeeItem feeItem = ccdResponse.getData().getOrderSummary().getFees().get(0);
        assertThat(feeItem.getValue().getFeeDescription(), is("desc"));
        assertThat(feeItem.getValue().getFeeVersion(), is("v1"));
        assertThat(feeItem.getValue().getFeeCode(), is("code1"));
        assertThat(feeItem.getValue().getFeeAmount(), is("1000"));

        assertThat(ccdResponse.getErrors(), hasItems("error1", "error2"));
        assertThat(ccdResponse.getWarnings(), hasItems("warning1", "warning2"));
    }
}