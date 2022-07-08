package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestorderapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ContestOrderApprovedLetterDetailsMapper {

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final CourtDetailsMapper courtDetailsMapper;
    private final ObjectMapper objectMapper;

    public ContestOrderApprovedLetterDetails buildContestOrderApprovedLetterDetails(FinremCaseDetails caseDetails,
                                                                                    CourtListWrapper courtList) {
        return ContestOrderApprovedLetterDetails.builder()
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .court(courtDetailsMapper.getCourtDetails(courtList).getCourtName())
            .judgeDetails(getJudgeDetails(caseDetails))
            .build();
    }

    public Map<String, Object> getConsentOrderApprovedLetterDetailsAsMap(FinremCaseDetails caseDetails,
                                                                         CourtListWrapper courtList) {
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Object> consentOrderApprovedLetterDetailsMap = objectMapper.convertValue(
            buildContestOrderApprovedLetterDetails(caseDetails, courtList),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, consentOrderApprovedLetterDetailsMap,
            "id", caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private String getJudgeDetails(FinremCaseDetails caseDetails) {
        return StringUtils.joinWith(" ",
            caseDetails.getCaseData().getOrderApprovedJudgeType().getValue(),
            caseDetails.getCaseData().getOrderApprovedJudgeName());
    }
}
