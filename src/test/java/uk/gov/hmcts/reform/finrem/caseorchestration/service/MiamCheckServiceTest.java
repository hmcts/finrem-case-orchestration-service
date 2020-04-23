package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLAIMING_EXEMPTION_MIAM;

public class MiamCheckServiceTest {

    private MiamCheckService service = new MiamCheckService();

    @Test
    public void miamCheckFailError() {
        Map<String, Object> caseData = ImmutableMap.of(APPLICANT_ATTENDED_MIAM, NO_VALUE, CLAIMING_EXEMPTION_MIAM, NO_VALUE);
        List<String> errors = service.miamExemptAttendCheck(CaseDetails.builder().data(caseData).build());
        assertThat(errors, hasSize(1));
        assertThat(errors,
            hasItems("You cannot make this application unless the applicant has either attended, or is exempt from attending a MIAM"));
    }

    @Test
    public void noError() {
        Map<String, Object> caseData = ImmutableMap.of(APPLICANT_ATTENDED_MIAM, NO_VALUE, CLAIMING_EXEMPTION_MIAM, YES_VALUE);
        List<String> errors = service.miamExemptAttendCheck(CaseDetails.builder().data(caseData).build());
        assertThat(errors, hasSize(0));
    }

    @Test
    public void noMiamRelatedInfo() {
        List<String> errors = service.miamExemptAttendCheck(CaseDetails.builder().data(ImmutableMap.of()).build());
        assertThat(errors, hasSize(0));
    }
}