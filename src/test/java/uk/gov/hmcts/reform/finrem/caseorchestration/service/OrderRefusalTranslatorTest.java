package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_COLLECTION;

public class OrderRefusalTranslatorTest extends BaseServiceTest {

    @Autowired OrderRefusalTranslatorService orderRefusalTranslatorService;

    private CaseDetails caseDetails;

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                     getClass().getResourceAsStream(fileName)) {
            caseDetails = mapper.readValue(resourceAsStream, CaseDetails.class);
        }
    }

    @Test
    public void shouldTranslateOrderRefusalCollection() throws Exception {
        setUpCaseDetails("/fixtures/model/case-details-multiple-orders.json");

        CaseDetails result = orderRefusalTranslatorService.translateOrderRefusalCollection(caseDetails);
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

        Map<String, Object> data = orderRefusalTranslatorService.copyToOrderRefusalCollection(caseDetails);

        List<OrderRefusalData> orderRefusalData = orderRefusalDataList(data, ORDER_REFUSAL_COLLECTION);
        List<String> orderRefusal = orderRefusalData.get(1).getOrderRefusal().getOrderRefusal();

        assertThat(orderRefusalData.size(), is(2));
        assertThat(orderRefusal, hasItems(
                "Insufficient information provided – A",
                "Insufficient information provided – B",
                "Transferred to Applicant’s home Court - A",
                "Transferred to Applicant's home Court - B",
                "Other"));
    }

    @Test
    public void shouldReturnOrderRefusalCollectionNewWhenOrderRefusalCollectionIsEmpty() throws Exception {
        setUpCaseDetails("/fixtures/model/copy-order-refusal-collection-empty.json");

        Map<String, Object> data = orderRefusalTranslatorService.copyToOrderRefusalCollection(caseDetails);

        List<OrderRefusalData> orderRefusalData = orderRefusalDataList(data, ORDER_REFUSAL_COLLECTION);
        List<String> orderRefusal = orderRefusalData.get(0).getOrderRefusal().getOrderRefusal();

        assertThat(orderRefusalData.size(), is(1));
        assertThat(orderRefusal, hasItems(
                "Insufficient information provided – A",
                "Insufficient information provided – B",
                "Transferred to Applicant’s home Court - A",
                "Transferred to Applicant's home Court - B",
                "Other"));
    }

    private List<OrderRefusalData> orderRefusalDataList(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field), new TypeReference<>() {});
    }
}
