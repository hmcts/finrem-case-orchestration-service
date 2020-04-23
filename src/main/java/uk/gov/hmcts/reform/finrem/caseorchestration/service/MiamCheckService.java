package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLAIMING_EXEMPTION_MIAM;

@Service
public class MiamCheckService {

    private static final String MIAM_EXEMPT_ERROR = "You cannot make this application unless the applicant has "
            + "either attended, or is exempt from attending a MIAM";

    public List<String> miamExemptAttendCheck(CaseDetails  caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String applicantAttended = Objects.toString(caseData.get(APPLICANT_ATTENDED_MIAM));
        String claimingExemption = Objects.toString(caseData.get(CLAIMING_EXEMPTION_MIAM));

        if (applicantAttended.equalsIgnoreCase("no")
                && claimingExemption.equalsIgnoreCase("no")) {
            return  ImmutableList.of(MIAM_EXEMPT_ERROR);
        }
        return ImmutableList.of();
    }
}
