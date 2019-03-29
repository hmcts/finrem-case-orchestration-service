package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusal;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderFunctions.*;

public class ConsentOrderFunctionsTest {

    @Test
    public void modifyOrderRefusalCollection() {
        OrderRefusal orderRefusal  = new OrderRefusal();
        orderRefusal.setOrderRefusal(
                ImmutableList.of("Insufficient information provided – A", "Insufficient information provided – B",
                        "Insufficient information provided – E", "Transferred to Applicant’s home Court",
                        "Transferred to Applicant's home Court"));
        OrderRefusalData orderRefusalData = new OrderRefusalData();
        orderRefusalData.setId("1");
        orderRefusalData.setOrderRefusal(orderRefusal);

        List<OrderRefusalData> orderRefusalCollection = ImmutableList.of(orderRefusalData);

        CaseData caseData = CaseData.builder().orderRefusalCollection(orderRefusalCollection).build();
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();


        CaseDetails result = translateOrderRefusalCollection(caseDetails);
        assertThat(result.getCaseData().getOrderRefusalCollection(), hasSize(1));
    }
}