package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_ATTENDANCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_EXEMPTION;

public class MiamCheckServiceTest {

    private MiamCheckService service = new MiamCheckService();

    @Test
    public void miamcheckFailError() {
        Map<String, Object> caseData = ImmutableMap.of(MIAM_ATTENDANCE, "No", MIAM_EXEMPTION, "No");
        List<String> errors = service.miamExemptAttendCheck(CaseDetails.builder().data(caseData).build());
        assertThat(errors, hasSize(1));
        assertThat(errors, hasItems("You cannot make this application unless the applicant has "
                + "either attended, or is exempt from attending a MIAM"));
    }

    @Test
    public void noError() {
        Map<String, Object> caseData = ImmutableMap.of(MIAM_ATTENDANCE, "No", MIAM_EXEMPTION, "Yes");
        List<String> errors = service.miamExemptAttendCheck(CaseDetails.builder().data(caseData).build());
        assertThat(errors, hasSize(0));
    }

    @Test
    public void noMiamRelatedInfo() {
        List<String> errors = service.miamExemptAttendCheck(CaseDetails.builder().data(ImmutableMap.of()).build());
        assertThat(errors, hasSize(0));
    }
}