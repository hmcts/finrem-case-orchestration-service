package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalorder;

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
public class GeneralOrderDetailsMapper {

    private static final String GENERAL_ORDER_COURT_CONSENTED = "SITTING in private";
    private static final String GENERAL_ORDER_COURT_SITTING = "SITTING AT the Family Court at the ";
    private static final String GENERAL_ORDER_HEADER_ONE = "In the Family Court";
    private static final String GENERAL_ORDER_HEADER_ONE_CONSENTED = "Sitting in the Family Court";
    private static final String GENERAL_ORDER_HEADER_TWO = "sitting in the";

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final CourtDetailsMapper courtDetailsMapper;
    private final ObjectMapper objectMapper;

    public GeneralOrderDetails buildGeneralOrderDetails(FinremCaseDetails caseDetails,
                                                        CourtListWrapper courtList) {
        return GeneralOrderDetails.builder()
            .divorceCaseNumber(caseDetails.getCaseData().getDivorceCaseNumber())
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .generalOrderCourtSitting(GENERAL_ORDER_COURT_SITTING)
            .generalOrderHeaderOne(getHeaderOne(caseDetails))
            .generalOrderHeaderTwo(GENERAL_ORDER_HEADER_TWO)
            .generalOrderCourt(getGeneralOrderCourt(caseDetails, courtList))
            .generalOrderJudgeDetails(getGeneralOrderJudgeDetails(caseDetails))
            .generalOrderRecitals(caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderRecitals())
            .generalOrderDate(String.valueOf(caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderDate()))
            .generalOrderBodyText(caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderBodyText())
            .build();
    }

    public Map<String, Object> getGeneralOrderDetailsAsMap(FinremCaseDetails caseDetails,
                                                           CourtListWrapper courtList) {
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Object> generalOrderDetailsMap = objectMapper.convertValue(buildGeneralOrderDetails(caseDetails, courtList),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, generalOrderDetailsMap,
            "id", caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private String getHeaderOne(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isConsentedApplication()
            ? GENERAL_ORDER_HEADER_ONE_CONSENTED
            : GENERAL_ORDER_HEADER_ONE;
    }

    private String getGeneralOrderCourt(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        return caseDetails.getCaseData().isConsentedApplication()
            ? GENERAL_ORDER_COURT_CONSENTED
            : courtDetailsMapper.getCourtDetails(courtList).getCourtName();
    }

    private String getGeneralOrderJudgeDetails(FinremCaseDetails caseDetails) {
        return StringUtils.joinWith(" ",
            caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderJudgeType().getValue(),
            caseDetails.getCaseData().getGeneralOrderWrapper().getGeneralOrderJudgeName());
    }
}
