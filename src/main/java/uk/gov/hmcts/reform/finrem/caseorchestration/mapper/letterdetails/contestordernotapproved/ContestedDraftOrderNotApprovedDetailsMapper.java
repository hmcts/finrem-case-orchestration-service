package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ContestedDraftOrderNotApprovedDetailsMapper {

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final CourtDetailsMapper courtDetailsMapper;
    private final ObjectMapper objectMapper;

    public ContestedDraftOrderNotApprovedDetails buildContestedDraftOrderNotApprovedDetails(FinremCaseDetails caseDetails,
                                                                                            CourtListWrapper courtList) {
        return ContestedDraftOrderNotApprovedDetails.builder()
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .court(courtDetailsMapper.getCourtDetails(courtList).getCourtName())
            .judgeDetails(getJudgeDetails(caseDetails))
            .contestOrderNotApprovedRefusalReasons(getFormattedRefusalReasons(caseDetails))
            .build();
    }

    public Map<String, Object> getConsentOrderApprovedLetterDetailsAsMap(FinremCaseDetails caseDetails,
                                                                         CourtListWrapper courtList) {
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Object> contestedDraftOrderNotApprovedDetails = objectMapper.convertValue(
            buildContestedDraftOrderNotApprovedDetails(caseDetails, courtList),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, contestedDraftOrderNotApprovedDetails,
            "id", caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private String getJudgeDetails(FinremCaseDetails caseDetails) {
        return StringUtils.joinWith(" ",
            caseDetails.getCaseData().getRefusalOrderJudgeType().getValue(),
            caseDetails.getCaseData().getRefusalOrderJudgeName());
    }

    private String getFormattedRefusalReasons(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<String> refusalReasons = caseData.getJudgeNotApprovedReasons().stream()
            .map(reason -> reason.getValue().getJudgeNotApprovedReasons())
            .collect(Collectors.toList());

        StringBuilder formattedRefusalReasons = new StringBuilder();
        refusalReasons.forEach(reason -> {
            if (formattedRefusalReasons.length() > 0) {
                formattedRefusalReasons.append('\n');
            }
            formattedRefusalReasons.append("- ");
            formattedRefusalReasons.append(reason);
        });
        return formattedRefusalReasons.toString();
    }
}
