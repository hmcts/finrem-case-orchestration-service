package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderRefusalTranslator.copyToOrderRefusalCollection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderRefusalTranslator.translateOrderRefusalCollection;

public class OrderRefusalTranslatorTest {

    private CaseDetails caseDetails;
    private ObjectMapper mapper = new ObjectMapper();

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                     getClass().getResourceAsStream(fileName)) {
            caseDetails = mapper.readValue(resourceAsStream, CaseDetails.class);
        }
    }

    @Test
    public void shouldTranslateOrderRefusalCollection() throws Exception {
        setUpCaseDetails("/fixtures/model/case-details-multiple-orders.json");

        CaseDetails result = translateOrderRefusalCollection(caseDetails);
        Map<String, Object> data = result.getData();
        List<OrderRefusalData> orderRefusalData = orderRefusalDataList(data, "orderRefusalCollectionNew");
        List<String> orderRefusal = orderRefusalData.get(0).getOrderRefusal().getOrderRefusal();

        assertThat(orderRefusal, hasItems(
                "Insufficient information provided – A",
                "Insufficient information provided – B",
                "Transferred to Applicant home Court - A",
                "Transferred to Applicant home Court - B",
                "Other"));
    }

    @Test
    public void shouldCopyToOrderRefusalCollection() throws Exception {
        setUpCaseDetails("/fixtures/model/copy-case-details-multiple-orders.json");

        Map<String, Object> data = copyToOrderRefusalCollection(caseDetails);

        List<OrderRefusalData> orderRefusalData = orderRefusalDataList(data, "orderRefusalCollection");
        List<String> orderRefusal = orderRefusalData.get(0).getOrderRefusal().getOrderRefusal();

        assertThat(orderRefusalData.size(), is(2));
        assertThat(orderRefusal, hasItems(
                "Insufficient information provided – A",
                "Insufficient information provided – B",
                "Transferred to Applicant’s home Court - A",
                "Transferred to Applicant's home Court - B",
                "Other"));
    }

    private List<OrderRefusalData> orderRefusalDataList(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
                new TypeReference<List<OrderRefusalData>>() {
                });
    }
}