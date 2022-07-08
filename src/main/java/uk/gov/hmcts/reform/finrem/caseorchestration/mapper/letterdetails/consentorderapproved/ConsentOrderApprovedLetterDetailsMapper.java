package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@RequiredArgsConstructor
@Component
public class ConsentOrderApprovedLetterDetailsMapper {

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final ObjectMapper objectMapper;

    public ConsentOrderApprovedLetterDetails buildConsentOrderApprovedLetterDetails(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        return ConsentOrderApprovedLetterDetails.builder()
            .civilPartnership(caseData.getCivilPartnership().getYesOrNo())
            .applicantFirstName(caseData.getContactDetailsWrapper().getApplicantFmName())
            .applicantLastName(caseData.getContactDetailsWrapper().getApplicantLname())
            .respondentFirstName(getRespondentFirstName(caseDetails))
            .respondentLastName(getRespondentLastName(caseDetails))
            .servePensionProviderResponsibility(caseData.getServePensionProviderResponsibility().getValue())
            .servePensionProvider(caseData.getServePensionProvider().getYesOrNo())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .orderDirectionDate(getDirectionDate(caseDetails))
            .orderDirectionJudge(getJudgeTitle(caseDetails))
            .orderDirectionJudgeName(getJudgeName(caseDetails))
            .servePensionProviderOther(nullToEmpty(caseData.getServePensionProviderOther()))
            .build();
    }

    public Map<String, Object> getConsentOrderApprovedLetterDetailsAsMap(FinremCaseDetails caseDetails) {
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Object> consentOrderApprovedLetterDetailsMap = objectMapper.convertValue(
            buildConsentOrderApprovedLetterDetails(caseDetails),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, consentOrderApprovedLetterDetailsMap,
            "id", caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    private String getRespondentFirstName(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? caseDetails.getCaseData().getContactDetailsWrapper().getRespondentFmName()
            : caseDetails.getCaseData().getContactDetailsWrapper().getAppRespondentFmName();
    }

    private String getRespondentLastName(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? caseDetails.getCaseData().getContactDetailsWrapper().getRespondentLname()
            : caseDetails.getCaseData().getContactDetailsWrapper().getAppRespondentLName();
    }

    private String getJudgeTitle(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? caseDetails.getCaseData().getConsentOrderWrapper().getConsentSelectJudge()
            : caseDetails.getCaseData().getOrderDirectionJudge().getValue();
    }

    private String getJudgeName(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? caseDetails.getCaseData().getConsentOrderWrapper().getConsentJudgeName()
            : caseDetails.getCaseData().getOrderDirectionJudgeName();
    }

    private String getDirectionDate(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseData().isContestedApplication()
            ? String.valueOf(caseDetails.getCaseData().getConsentOrderWrapper().getConsentDateOfOrder())
            : String.valueOf(caseDetails.getCaseData().getOrderDirectionDate());
    }
}
