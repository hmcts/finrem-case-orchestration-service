package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormCLetterDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.time.LocalDate;

@Component
public class FormCLetterDetailsMapper extends AbstractLetterDetailsMapper {
    public FormCLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        super(courtDetailsMapper, objectMapper);
    }

    @Override
    public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails, CourtListWrapper courtList) {
        FinremCaseData caseData = caseDetails.getCaseData();
        LocalDate hearingDate = caseData.getHearingDate();
        return FormCLetterDetails.builder()
            .applicantFmName(caseData.getContactDetailsWrapper().getApplicantFmName())
            .applicantLName(caseData.getContactDetailsWrapper().getApplicantLname())
            .respondentFmName(caseData.getContactDetailsWrapper().getRespondentFmName())
            .respondentLName(caseData.getContactDetailsWrapper().getRespondentLname())
            .divorceCaseNumber(caseData.getDivorceCaseNumber())
            .courtDetails(courtDetailsMapper.getCourtDetails(courtList))
            .hearingDate(String.valueOf(hearingDate))
            .hearingDateLess35Days(String.valueOf(hearingDate.minusDays(35)))
            .hearingDateLess14Days(String.valueOf(hearingDate.minusDays(14)))
            .solicitorReference(caseData.getContactDetailsWrapper().getSolicitorReference())
            .respondentSolicitorReference(caseData.getContactDetailsWrapper().getRespondentSolicitorReference())
            .additionalInformationAboutHearing(caseData.getAdditionalInformationAboutHearing())
            .hearingTime(caseData.getHearingTime())
            .timeEstimate(caseData.getTimeEstimate())
            .formCCreatedDate(String.valueOf(LocalDate.now()))
            .eventDatePlus21Days(String.valueOf(LocalDate.now().plusDays(21)))
            .build();
    }
}
