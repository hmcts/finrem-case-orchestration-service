package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.consentorderapproved;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
public class ConsentOrderApprovedLetterDetailsMapper extends AbstractLetterDetailsMapper {

    public ConsentOrderApprovedLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
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
