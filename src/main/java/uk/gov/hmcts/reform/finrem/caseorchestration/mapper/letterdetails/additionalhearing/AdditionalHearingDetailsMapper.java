package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.additionalhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class AdditionalHearingDetailsMapper {

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final CourtDetailsMapper courtDetailsMapper;
    private final ObjectMapper objectMapper;

    public AdditionalHearingDetails buildAdditionalHearingDetails(FinremCaseDetails caseDetails,
                                                                  CourtListWrapper courtList) {
        FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        return AdditionalHearingDetails.builder()
            .ccdCaseNumber(String.valueOf(caseDetails.getId()))
            .divorceCaseNumber(caseDetails.getCaseData().getDivorceCaseNumber())
            .applicantName(caseDetails.getCaseData().getFullApplicantName())
            .respondentName(caseDetails.getCaseData().getRespondentFullName())
            .courtName(courtDetails.getCourtName())
            .courtAddress(courtDetails.getCourtAddress())
            .courtEmail(courtDetails.getEmail())
            .courtPhone(courtDetails.getPhoneNumber())
            .hearingDate(String.valueOf(caseDetails.getCaseData().getHearingDate()))
            .hearingType(caseDetails.getCaseData().getHearingType().getId())
            .hearingVenue(courtDetails.getCourtName())
            .hearingLength(caseDetails.getCaseData().getTimeEstimate())
            .hearingTime(caseDetails.getCaseData().getHearingTime())
            .additionalHearingDated(new Date())
            .build();
    }

    public Map<String, Object> getAdditionalHearingDetailsAsMap(FinremCaseDetails caseDetails,
                                                                CourtListWrapper courtList) {
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Object> additionalHearingDetailsMap = objectMapper.convertValue(
            buildAdditionalHearingDetails(caseDetails, courtList),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, additionalHearingDetailsMap,
            "id", caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }
}
