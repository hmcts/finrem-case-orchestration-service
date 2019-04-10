package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderRefusalTranslator.translateOrderRefusalCollection;

public class OrderRefusalTranslatorTest {

    private CaseDetails caseDetails;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUpCaseDetails() throws Exception {
        try (InputStream resourceAsStream =
                     getClass().getResourceAsStream("/fixtures/model/case-details-multiple-orders.json")) {
            caseDetails = mapper.readValue(resourceAsStream, CaseDetails.class);
        }
    }

    @Test
    public void modifyOrderRefusalCollection() {
        CaseDetails result = translateOrderRefusalCollection(caseDetails);

        OrderRefusalData orderRefusalData = orderRefusalDataList(result);
        List<String> orderRefusal = orderRefusalData.getOrderRefusal().getOrderRefusal();

        assertThat(orderRefusal, hasItems(
                "Insufficient information provided – A",
                "Insufficient information provided – B",
                "Transferred to Applicant home Court - A",
                "Transferred to Applicant home Court - B",
                "Other"));
    }

    private OrderRefusalData orderRefusalDataList(CaseDetails result) {
        List<OrderRefusalData> list =
                mapper.convertValue(result.getData().get("orderRefusalCollection"),
                        new TypeReference<List<OrderRefusalData>>() {});

        return list.get(0);
    }
}